/*
 * Copyright (C) 2011-2023 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv;

import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.InventoryAccess;
import com.lishid.openinv.util.Permissions;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listener for inventory-related events to prevent modification of inventories where not allowed.
 *
 * @author Jikoo
 */
record InventoryListener(OpenInv plugin) implements Listener {

    @EventHandler
    private void onInventoryClose(@NotNull final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (this.plugin.getSilentContainerStatus(player)
                && holder != null
                && this.plugin.getAnySilentContainer().isAnySilentContainer(holder)) {
            this.plugin.getAnySilentContainer().deactivateContainer(player);
        }

        ISpecialInventory specialInventory = InventoryAccess.getEnderChest(event.getInventory());
        if (specialInventory != null) {
            this.plugin.handleCloseInventory(event.getPlayer(), specialInventory);
        } else {
            specialInventory = InventoryAccess.getPlayerInventory(event.getInventory());
            if (specialInventory != null) {
                this.plugin.handleCloseInventory(event.getPlayer(), specialInventory);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onInventoryClick(@NotNull final InventoryClickEvent event) {
        if (handleInventoryInteract(event)) {
            return;
        }

        // Safe cast - has to be a player to be the holder of a special player inventory.
        Player player = (Player) event.getWhoClicked();

        if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // All own-inventory interactions require updates to display properly.
            // Update in same tick after event completion.
            this.plugin.getServer().getScheduler().runTask(this.plugin, player::updateInventory);
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
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            player.getInventory().addItem(clone);
            player.updateInventory();
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onInventoryDrag(@NotNull final InventoryDragEvent event) {
        if (handleInventoryInteract(event)) {
            return;
        }

        InventoryView view = event.getView();
        int topSize = view.getTopInventory().getSize();

        // Get bottom inventory active slots as player inventory slots.
        Set<Integer> slots = event.getRawSlots().stream()
                .filter(slot -> slot >= topSize)
                .map(slot -> plugin.convertToPlayerSlot(view, slot)).collect(Collectors.toSet());

        int overlapLosses = 0;

        // Count overlapping slots.
        for (Map.Entry<Integer, ItemStack> newItem : event.getNewItems().entrySet()) {
            int rawSlot = newItem.getKey();

            // Skip bottom inventory slots.
            if (rawSlot >= topSize) {
                continue;
            }

            int convertedSlot = plugin.convertToPlayerSlot(view, rawSlot);

            if (slots.contains(convertedSlot)) {
                overlapLosses += getCountDiff(view.getItem(rawSlot), newItem.getValue());
            }
        }

        // Allow no overlap to proceed as usual.
        if (overlapLosses < 1) {
            return;
        }

        ItemStack cursor = event.getCursor();
        if (cursor != null) {
            cursor.setAmount(cursor.getAmount() + overlapLosses);
        } else {
            cursor = event.getOldCursor().clone();
            cursor.setAmount(overlapLosses);
        }

        event.setCursor(cursor);
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
        HumanEntity entity = event.getWhoClicked();

        // Un-cancel spectator interactions.
        if (Permissions.SPECTATE.hasPermission(entity) && entity.getGameMode() == GameMode.SPECTATOR) {
            event.setCancelled(false);
        }

        if (event.isCancelled()) {
            return true;
        }

        Inventory inventory = event.getView().getTopInventory();

        // Is the inventory a special ender chest?
        if (InventoryAccess.isEnderChest(inventory)) {
            // Disallow ender chest interaction for users without edit permission.
            if (!Permissions.EDITENDER.hasPermission(entity)) {
                event.setCancelled(true);
            }
            return true;
        }

        ISpecialPlayerInventory playerInventory = InventoryAccess.getPlayerInventory(inventory);

        // Ignore inventories other than special player inventories.
        if (playerInventory == null) {
            return true;
        }

        // Disallow player inventory interaction for users without edit permission.
        if (!Permissions.EDITINV.hasPermission(entity)) {
            event.setCancelled(true);
            return true;
        }

        // Only specially handle actions in the player's own inventory.
        return !event.getWhoClicked().equals(playerInventory.getPlayer());
    }

}
