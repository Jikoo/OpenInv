package com.lishid.openinv.listener;

import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.AccessEqualMode;
import com.lishid.openinv.util.InventoryAccess;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.config.Config;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A listener used to enable functionality and prevent issues on versions < 1.21.
 */
public class LegacyInventoryListener implements Listener {

  private final @NotNull Plugin plugin;
  private final @NotNull Config config;

  public LegacyInventoryListener(@NotNull Plugin plugin, @NotNull Config config) {
    this.plugin = plugin;
    this.config = config;
  }

  @Keep
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  private void onInventoryClick(@NotNull final InventoryClickEvent event) {
    if (handleInventoryInteract(event)) {
      return;
    }

    // Safe cast - has to be a player to be the holder of a special player inventory.
    Player player = (Player) event.getWhoClicked();

    if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
      return;
    }

    // Extra handling for MOVE_TO_OTHER_INVENTORY - apparently Mojang no longer removes the item from the target
    // inventory prior to adding it to existing stacks.
    ItemStack currentItem = event.getCurrentItem();
    if (currentItem == null) {
      // Other plugin doing some sort of handling (would be NOTHING for null item otherwise), ignore.
      return;
    }

    ItemStack clone = currentItem.clone();
    event.setCurrentItem(null);

    // Complete add action in same tick after event completion.
    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> player.getInventory().addItem(clone));
  }

  @Keep
  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  private void onInventoryDrag(@NotNull final InventoryDragEvent event) {
    if (handleInventoryInteract(event)) {
      return;
    }

    InventoryView view = event.getView();

    if (view.getCursor() == null) {
      return;
    }

    int topSize = view.getTopInventory().getSize();

    // Get bottom inventory active slots as player inventory slots.
    Set<Integer> slots = event.getRawSlots().stream()
        .filter(slot -> slot >= topSize)
        .map(slot -> convertToPlayerSlot(view, slot)).collect(Collectors.toSet());

    int overlapLosses = 0;

    // Count overlapping slots.
    for (Map.Entry<Integer, ItemStack> newItem : event.getNewItems().entrySet()) {
      int rawSlot = newItem.getKey();

      // Skip bottom inventory slots.
      if (rawSlot >= topSize) {
        continue;
      }

      int convertedSlot = convertToPlayerSlot(view, rawSlot);

      if (slots.contains(convertedSlot)) {
        overlapLosses += getCountDiff(view.getItem(rawSlot), newItem.getValue());
      }
    }

    // Allow no overlap to proceed as usual.
    if (overlapLosses < 1) {
      return;
    }

    final ItemStack lost = view.getCursor().clone();
    lost.setAmount(overlapLosses);

    // Re-add the lost items in the same tick after the event has completed.
    plugin.getServer().getScheduler().runTask(plugin, () -> {
      InventoryView currentOpen = event.getWhoClicked().getOpenInventory();

      if (!currentOpen.equals(view)) {
        event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), lost).setPickupDelay(0);
        return;
      }

      ItemStack cursor = currentOpen.getCursor();

      if (cursor == null) {
        currentOpen.setCursor(lost);
      } else if (lost.isSimilar(cursor)) {
        cursor.setAmount(cursor.getAmount() + lost.getAmount());
        currentOpen.setCursor(cursor);
      } else {
        event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), lost).setPickupDelay(0);
      }
    });
  }

  private int getCountDiff(@Nullable ItemStack original, @NotNull ItemStack result) {
    if (original == null || original.getType() != result.getType()) {
      return result.getAmount();
    }

    return result.getAmount() - original.getAmount();
  }

  /**
   * Handle common InventoryInteractEvent functions.
   *
   * @param event the InventoryInteractEvent
   * @return true unless the top inventory is the holder's own inventory
   */
  private boolean handleInventoryInteract(@NotNull final InventoryInteractEvent event) {
    HumanEntity viewer = event.getWhoClicked();

    Inventory inventory = event.getView().getTopInventory();
    ISpecialInventory backing = InventoryAccess.getInventory(inventory);

    if (backing == null) {
      return true;
    }

    Permissions edit;
    HumanEntity target = backing.getPlayer();
    boolean ownContainer = viewer.equals(target);
    if (backing instanceof ISpecialPlayerInventory) {
      edit = ownContainer ? Permissions.INVENTORY_EDIT_SELF : Permissions.INVENTORY_EDIT_OTHER;
    } else if (backing instanceof ISpecialEnderChest) {
      edit = ownContainer ? Permissions.ENDERCHEST_EDIT_SELF : Permissions.ENDERCHEST_OPEN_OTHER;
    } else {
      // Unknown implementation.
      return true;
    }

    if (!edit.hasPermission(viewer)) {
      event.setCancelled(true);
      return true;
    }

    // If access ties aren't view-only mode, don't bother with permission checks.
    if (config.getAccessEqualMode() != AccessEqualMode.VIEW) {
      return !ownContainer || !(backing instanceof ISpecialPlayerInventory);
    }

    for (int level = 4; level > 0; --level) {
      String permission = "openinv.access.level." + level;
      // If the target doesn't have this access level...
      if (!target.hasPermission(permission)) {
        // If the viewer does have the access level, all good.
        if (viewer.hasPermission(permission)) {
          break;
        }
        // Otherwise check next access level.
        continue;
      }

      // Either the viewer lacks access (which shouldn't be possible) or this is a tie. View-only.
      event.setCancelled(true);
      return true;
    }

    return !ownContainer || !(backing instanceof ISpecialPlayerInventory);
  }

  private static int convertToPlayerSlot(InventoryView view, int rawSlot) {
    int topSize = view.getTopInventory().getSize();
    if (topSize <= rawSlot) {
      // Slot is not inside special inventory, use Bukkit logic.
      return view.convertSlot(rawSlot);
    }

    // Main inventory, slots 0-26 -> 9-35
    if (rawSlot < 27) {
      return rawSlot + 9;
    }
    // Hotbar, slots 27-35 -> 0-8
    if (rawSlot < 36) {
      return rawSlot - 27;
    }
    // Armor, slots 36-39 -> 39-36
    if (rawSlot < 40) {
      return 36 + (39 - rawSlot);
    }
    // Off-hand
    if (rawSlot == 40) {
      return 40;
    }
    // Drop slots, "out of inventory"
    return -1;
  }

}
