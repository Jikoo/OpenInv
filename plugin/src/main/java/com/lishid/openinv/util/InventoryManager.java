package com.lishid.openinv.util;

import com.github.jikoo.planarwrappers.util.version.BukkitVersions;
import com.github.jikoo.planarwrappers.util.version.Version;
import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.OpenInv;
import com.lishid.openinv.event.OpenEvents;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.config.Config;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * A manager for special inventories. Delegates creation and tracks copies in use.
 */
public class InventoryManager implements Listener {

  private final Map<UUID, ISpecialPlayerInventory> inventories = new ConcurrentHashMap<>();
  private final Map<UUID, ISpecialEnderChest> enderChests = new ConcurrentHashMap<>();
  private final Set<UUID> expectedCloses = new HashSet<>();
  private final @NotNull OpenInv plugin;
  private final @NotNull Config config;
  private final @NotNull InternalAccessor accessor;

  public InventoryManager(@NotNull OpenInv plugin, @NotNull Config config, @NotNull InternalAccessor accessor) {
    this.plugin = plugin;
    this.config = config;
    this.accessor = accessor;
  }

  public void evictAll() {
    Stream.concat(inventories.values().stream(), enderChests.values().stream())
        .map(inventory -> {
          // Rather than iterate twice, evict all viewers during remapping.
          for (HumanEntity viewer : List.copyOf(inventory.getBukkitInventory().getViewers())) {
            expectedCloses.add(viewer.getUniqueId());
            viewer.closeInventory();
          }
          // If saving is prevented, return a null value for the player to save.
          if (config.isSaveDisabled() || OpenEvents.saveCancelled(inventory)) {
            return null;
          }
          if (inventory.getPlayer() instanceof Player player) {
            return player;
          }
          return null;
        })
        .filter(Objects::nonNull)
        .distinct()
        .forEach(player -> {
          if (!player.isOnline()) {
            accessor.getPlayerDataManager().inject(player).saveData();
          }
        });
    inventories.clear();
    enderChests.clear();
    expectedCloses.clear();
  }

  public @NotNull ISpecialPlayerInventory getInventory(@NotNull Player player) {
    return inventories.computeIfAbsent(player.getUniqueId(), uuid -> accessor.createInventory(player));
  }

  public @NotNull ISpecialEnderChest getEnderChest(@NotNull Player player) {
    return enderChests.computeIfAbsent(player.getUniqueId(), uuid -> accessor.createEnderChest(player));
  }

  public @Nullable Player getLoadedPlayer(@NotNull UUID uuid) {
    ISpecialInventory inUse = inventories.get(uuid);
    if (inUse != null) {
      return (Player) inUse.getPlayer();
    }
    inUse = enderChests.get(uuid);
    if (inUse != null) {
      return (Player) inUse.getPlayer();
    }
    return null;
  }

  public void unload(@NotNull UUID uuid) {
    inventories.computeIfPresent(uuid, this::remove);
    enderChests.computeIfPresent(uuid, this::remove);
  }

  public void save(@NotNull UUID uuid) {
    consumeLoaded(uuid, inventory -> {});
  }

  @Keep
  @EventHandler(priority = EventPriority.LOWEST)
  private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    consumeLoaded(event.getPlayer().getUniqueId(), inventory -> {
      inventory.setPlayerOnline(event.getPlayer());
      checkViewerAccess(inventory, true);
    });
  }

  @Keep
  @EventHandler(priority = EventPriority.MONITOR)
  private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    consumeLoaded(event.getPlayer().getUniqueId(), inventory -> {
      inventory.setPlayerOffline();
      checkViewerAccess(inventory, false);
    });
  }

  @Keep
  @EventHandler
  private void onWorldChanged(@NotNull PlayerChangedWorldEvent event) {
    Player player = event.getPlayer();
    consumeLoaded(player.getUniqueId(), inventory -> checkViewerAccess(inventory, player.isOnline()));
  }

  @Keep
  @EventHandler
  private void onInventoryClose(@NotNull InventoryCloseEvent event) {
    ISpecialInventory inventory = InventoryAccess.getInventory(event.getInventory());

    // If this is not an ISpecialInventory or the inventory was closed elsewhere internally, don't handle.
    if (inventory == null || expectedCloses.remove(event.getPlayer().getUniqueId())) {
      return;
    }

    // Fetch the active ISpecialInventory of this type.
    Map<UUID, ? extends ISpecialInventory> map = inventory instanceof ISpecialPlayerInventory ? inventories : enderChests;
    UUID key = inventory.getPlayer().getUniqueId();
    ISpecialInventory loaded = map.get(key);

    // If there is no loaded inventory, it has already been removed and saved.
    if (loaded == null) {
      return;
    }

    // This should only be possible if a plugin is going to extreme lengths to mess with our inventories.
    if (loaded != inventory) {
      // Immediately remove affected inventory, then dump all viewers. We don't want to risk duplication bugs.
      map.remove(key);
      remove(key, loaded);
      remove(key, inventory);
      // The loaded one is "correct" as far as we're concerned, so save that.
      save(loaded);
    }

    // Schedule task to check in use status later this tick. Closing user is still in viewer list.
    plugin.getScheduler().runTask(() -> {
      if (loaded.isInUse()) {
        return;
      }

      // Re-fetch from map to reduce odds of a duplicate save.
      ISpecialInventory current = map.remove(key);

      if (current != null) {
        save(current);
      }
    });
  }

  @Keep
  @EventHandler(priority = EventPriority.HIGHEST)
  private void onInventoryOpen(@NotNull InventoryOpenEvent event) {
    ISpecialInventory inventory = InventoryAccess.getInventory(event.getInventory());
    if (inventory == null) {
      return;
    }

    Map<UUID, ? extends ISpecialInventory> map = inventory instanceof ISpecialPlayerInventory ? inventories : enderChests;
    UUID key = inventory.getPlayer().getUniqueId();
    ISpecialInventory loaded = map.get(key);

    if (!inventory.equals(loaded)) {
      event.setCancelled(true);
      plugin.getLogger().log(
          Level.WARNING,
          "Prevented a plugin from opening an untracked ISpecialInventory!",
          new Throwable("Untracked ISpecialInventory"));
    }
  }

  private <T extends ISpecialInventory> void checkViewerAccess(@NotNull T inventory, boolean online) {

    Player owner = (Player) inventory.getPlayer();
    Permissions connectedState = online ? Permissions.ACCESS_ONLINE : Permissions.ACCESS_OFFLINE;
    boolean alwaysDenied = !online && config.isOfflineDisabled();

    // Copy viewers so we don't modify the list we're iterating over when closing inventories.
    List<HumanEntity> viewers = new ArrayList<>(inventory.getBukkitInventory().getViewers());
    // Legacy: Owner is always a viewer of own inventory.
    if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 21))) {
      if (inventory instanceof ISpecialPlayerInventory) {
        Inventory active = owner.getOpenInventory().getTopInventory();
        if (!active.equals(inventory.getBukkitInventory())) {
          viewers.remove(owner);
        }
      }
    }

    for (HumanEntity viewer : viewers) {
      if (alwaysDenied
          || !connectedState.hasPermission(viewer)
          || (!Objects.equals(owner.getWorld(), viewer.getWorld()) && !Permissions.ACCESS_CROSSWORLD.hasPermission(viewer))) {
        expectedCloses.add(viewer.getUniqueId());
        viewer.closeInventory();
      }
    }
  }

  private void consumeLoaded(@NotNull UUID key, @NotNull Consumer<@NotNull ISpecialInventory> consumer) {
    boolean saved = consumeLoaded(inventories, key, false, consumer);
    consumeLoaded(enderChests, key, saved, consumer);
  }

  private <T extends ISpecialInventory> boolean consumeLoaded(
      @NotNull Map<UUID, T> map,
      @NotNull UUID key,
      boolean saved,
      @NotNull Consumer<@NotNull ISpecialInventory> consumer) {
    T inventory = map.get(key);

    if (inventory == null) {
      return saved;
    }

    consumer.accept(inventory);
    if (!inventory.isInUse()) {
      map.remove(key);

      if (!saved) {
        save(inventory);
        return true;
      }
    }

    return saved;
  }

  private void save(@NotNull ISpecialInventory inventory) {
    if (config.isSaveDisabled()) {
      return;
    }

    Player player = (Player) inventory.getPlayer();

    if (!player.isOnline() && !OpenEvents.saveCancelled(inventory)) {
      accessor.getPlayerDataManager().inject(player).saveData();
    }
  }

  @Contract("_, _ -> null")
  private <T extends ISpecialInventory> @Nullable T remove(@NotNull UUID key, @NotNull T inventory) {
    for (HumanEntity viewer : List.copyOf(inventory.getBukkitInventory().getViewers())) {
      expectedCloses.add(viewer.getUniqueId());
      viewer.closeInventory();
    }
    return null;
  }

}
