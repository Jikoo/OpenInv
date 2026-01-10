package com.lishid.openinv.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.OpenInv;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.profile.OfflinePlayerProfileStore;
import com.lishid.openinv.util.profile.Profile;
import com.lishid.openinv.util.profile.ProfileStore;
import com.lishid.openinv.util.profile.jdbc.JdbcProfileStore;
import com.lishid.openinv.util.profile.jdbc.SqliteProfileStore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility for looking up and loading players.
 */
public class PlayerLoader implements Listener {

  private final @NotNull OpenInv plugin;
  private final @NotNull Config config;
  private final @NotNull InventoryManager inventoryManager;
  private final @NotNull InternalAccessor internalAccessor;
  private final @NotNull Logger logger;
  private final @NotNull Cache<String, Profile> lookupCache;
  private @NotNull ProfileStore profileStore;

  public PlayerLoader(
      @NotNull OpenInv plugin,
      @NotNull Config config,
      @NotNull InventoryManager inventoryManager,
      @NotNull InternalAccessor internalAccessor,
      @NotNull Logger logger
  ) {
    this.plugin = plugin;
    this.config = config;
    this.inventoryManager = inventoryManager;
    this.internalAccessor = internalAccessor;
    try {
      JdbcProfileStore jdbcProfileStore = new SqliteProfileStore(plugin);
      jdbcProfileStore.setup();
      jdbcProfileStore.tryImport();
      this.profileStore = jdbcProfileStore;
    } catch (Exception e) {
      this.profileStore = new OfflinePlayerProfileStore(logger);
    }
    this.logger = logger;
    this.lookupCache = CacheBuilder.newBuilder().maximumSize(20).build();
  }

  public @NotNull ProfileStore getProfileStore() {
    return profileStore;
  }

  public void setProfileStore(@NotNull ProfileStore profileStore) {
    this.profileStore = profileStore;
  }

  /**
   * Load a {@link Player} from an {@link OfflinePlayer}. If the user has not played before or the default world for
   * the server is not loaded, this will return {@code null}.
   *
   * @param offline the {@code OfflinePlayer} to load a {@code Player} for
   * @return the loaded {@code Player}
   * @throws IllegalStateException if the server version is unsupported
   */
  public @Nullable Player load(@NotNull OfflinePlayer offline) {
    Player player = offline.getPlayer();
    if (player != null) {
      return player;
    }

    if (config.isOfflineDisabled() || !internalAccessor.isSupported()) {
      return null;
    }

    player = inventoryManager.getLoadedPlayer(offline.getUniqueId());
    if (player != null) {
      return player;
    }

    if (Bukkit.isPrimaryThread()) {
      return internalAccessor.getPlayerDataManager().loadPlayer(offline);
    }

    CompletableFuture<Player> future = new CompletableFuture<>();
    plugin.getScheduler().runTask(() -> future.complete(internalAccessor.getPlayerDataManager().loadPlayer(offline)));

    try {
      player = future.get();
    } catch (InterruptedException | ExecutionException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return null;
    }

    return player;
  }

  public @Nullable OfflinePlayer matchExact(@NotNull String name) {
    OfflinePlayer player;

    try {
      UUID uuid = UUID.fromString(name);
      player = Bukkit.getOfflinePlayer(uuid);
      // Ensure player is an existing player.
      if (player.hasPlayedBefore() || player.isOnline()) {
        return player;
      }
      // Return null otherwise.
      return null;
    } catch (IllegalArgumentException ignored) {
      // Not a UUID
    }

    // Exact online match first.
    player = Bukkit.getServer().getPlayerExact(name);

    if (player != null) {
      return player;
    }

    // Cached offline match.
    Profile cachedResult = lookupCache.getIfPresent(name);
    if (cachedResult != null) {
      player = Bukkit.getOfflinePlayer(cachedResult.id());
      // Ensure player is an existing player.
      if (player.hasPlayedBefore() || player.isOnline()) {
        return player;
      }
      // Return null otherwise.
      return null;
    }

    // Exact offline match second - ensure offline access works when matchable users are online.
    Profile profile = profileStore.getProfileExact(name);
    if (profile == null) {
      return null;
    }

    player = Bukkit.getOfflinePlayer(profile.id());

    if (player.hasPlayedBefore()) {
      lookupCache.put(name, profile);
      return player;
    }

    return null;
  }

  public @Nullable OfflinePlayer match(@NotNull String name) {
    OfflinePlayer player = this.matchExact(name);

    if (player != null) {
      return player;
    }

    // Inexact online match.
    player = Bukkit.getServer().getPlayer(name);

    if (player != null) {
      return player;
    }

    // Finally, inexact offline match.
    Profile profile = getProfileStore().getProfileInexact(name);

    if (profile == null) {
      // No match found.
      return null;
    }

    // Get associated player and store match.
    player = Bukkit.getOfflinePlayer(profile.id());
    lookupCache.put(name, profile);
    return player;
  }

  @Keep
  @EventHandler
  private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    plugin.getScheduler().runTaskLaterAsynchronously(() -> updateMatches(event), 7L);
  }

  private void updateMatches(@NotNull PlayerJoinEvent event) {
    // Update profile store.
    profileStore.addProfile(new Profile(event.getPlayer()));

    // If player is not new, any cached values are valid.
    if (event.getPlayer().hasPlayedBefore() || lookupCache.size() == 0) {
      return;
    }

    // New player may have a name that already points to someone else in lookup cache.
    String name = event.getPlayer().getName();
    lookupCache.invalidate(name);

    Iterator<Map.Entry<String, Profile>> iterator = lookupCache.asMap().entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Profile> entry = iterator.next();
      String oldMatch = entry.getValue().name();
      String lookup = entry.getKey();
      float oldMatchScore = StringMetric.compareJaroWinkler(lookup, oldMatch);
      float newMatchScore = StringMetric.compareJaroWinkler(lookup, name);

      // If new match exceeds old match, delete old match.
      if (newMatchScore > oldMatchScore) {
        iterator.remove();
      }
    }
  }

}
