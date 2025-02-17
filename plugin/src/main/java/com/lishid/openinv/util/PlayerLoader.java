package com.lishid.openinv.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.OpenInv;
import com.lishid.openinv.util.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
  private final @NotNull Cache<String, PlayerProfile> lookupCache;

  public PlayerLoader(
      @NotNull OpenInv plugin,
      @NotNull Config config,
      @NotNull InventoryManager inventoryManager,
      @NotNull InternalAccessor internalAccessor,
      @NotNull Logger logger) {
    this.plugin = plugin;
    this.config = config;
    this.inventoryManager = inventoryManager;
    this.internalAccessor = internalAccessor;
    this.logger = logger;
    this.lookupCache = CacheBuilder.newBuilder().maximumSize(20).build();
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
    UUID key = offline.getUniqueId();

    Player player = offline.getPlayer();
    if (player != null) {
      return player;
    }

    player = inventoryManager.getLoadedPlayer(key);
    if (player != null) {
      return player;
    }

    if (config.isOfflineDisabled() || !internalAccessor.isSupported()) {
      return null;
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

  public @Nullable OfflinePlayer match(@NotNull String name) {
    // Warn if called on the main thread - if we resort to searching offline players, this may take several seconds.
    if (Bukkit.getServer().isPrimaryThread()) {
      logger.warning("Call to PlayerSearchCache#matchPlayer made on the main thread!");
      logger.warning("This can cause the server to hang, potentially severely.");
      logger.log(Level.WARNING, "Current stack trace", new Throwable("Current stack trace"));
    }

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
    PlayerProfile cachedResult = lookupCache.getIfPresent(name);
    if (cachedResult != null && cachedResult.getUniqueId() != null) {
      player = Bukkit.getOfflinePlayer(cachedResult.getUniqueId());
      // Ensure player is an existing player.
      if (player.hasPlayedBefore() || player.isOnline()) {
        return player;
      }
      // Return null otherwise.
      return null;
    }

    // Exact offline match second - ensure offline access works when matchable users are online.
    player = Bukkit.getServer().getOfflinePlayer(name);

    if (player.hasPlayedBefore()) {
      lookupCache.put(name, player.getPlayerProfile());
      return player;
    }

    // Inexact online match.
    player = Bukkit.getServer().getPlayer(name);

    if (player != null) {
      return player;
    }

    // Finally, inexact offline match.
    float bestMatch = 0;
    for (OfflinePlayer offline : Bukkit.getServer().getOfflinePlayers()) {
      if (offline.getName() == null) {
        // Loaded by UUID only, name has never been looked up.
        continue;
      }

      float currentMatch = StringMetric.compareJaroWinkler(name, offline.getName());

      if (currentMatch == 1.0F) {
        return offline;
      }

      if (currentMatch > bestMatch) {
        bestMatch = currentMatch;
        player = offline;
      }
    }

    if (player != null) {
      // If a match was found, store it.
      lookupCache.put(name, player.getPlayerProfile());
      return player;
    }

    // No players have ever joined the server.
    return null;
  }

  @Keep
  @EventHandler
  private void updateMatches(@NotNull PlayerJoinEvent event) {
    // If player is not new, any cached values are valid.
    if (event.getPlayer().hasPlayedBefore()) {
      return;
    }

    // New player may have a name that already points to someone else in lookup cache.
    String name = event.getPlayer().getName();
    lookupCache.invalidate(name);

    // If the cache is empty, nothing to do. Don't hit scheduler.
    if (lookupCache.size() == 0) {
      return;
    }

    plugin.getScheduler().runTaskLaterAsynchronously(() -> {
      Iterator<Map.Entry<String, PlayerProfile>> iterator = lookupCache.asMap().entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, PlayerProfile> entry = iterator.next();
        String oldMatch = entry.getValue().getName();

        // Shouldn't be possible - all profiles should be complete.
        if (oldMatch == null) {
          iterator.remove();
          continue;
        }

        String lookup = entry.getKey();
        float oldMatchScore = StringMetric.compareJaroWinkler(lookup, oldMatch);
        float newMatchScore = StringMetric.compareJaroWinkler(lookup, name);

        // If new match exceeds old match, delete old match.
        if (newMatchScore > oldMatchScore) {
          iterator.remove();
        }
      }
    }, 7L);
  }

}
