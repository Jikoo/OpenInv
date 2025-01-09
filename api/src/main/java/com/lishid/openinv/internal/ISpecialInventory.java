/*
 * Copyright (C) 2011-2022 lishid. All rights reserved.
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

package com.lishid.openinv.internal;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Interface defining behavior for special inventories backed by other inventories' content listings.
 */
public interface ISpecialInventory {

    /**
     * Get the {@link Inventory} associated with this {@code ISpecialInventory}.
     *
     * @return the Bukkit inventory
     */
    @NotNull Inventory getBukkitInventory();

    /**
     * Get the {@link InventoryType} corresponding to this {@code ISpecialInventory}.
     *
     * @return the type of Bukkit inventory
     */
    @NotNull InventoryType getBukkitType();

    /**
     * Set the owning {@link Player} instance to a newly-joined user.
     *
     * @param player the user coming online
     */
    void setPlayerOnline(@NotNull Player player);

    /**
     * Mark the owner of the inventory offline.
     */
    void setPlayerOffline();

    /**
     * Get whether the inventory is being viewed by any users.
     *
     * @return true if the inventory is being viewed
     */
    default boolean isInUse() {
        return !getBukkitInventory().getViewers().isEmpty();
    }

    /**
     * Get the {@link Player} who owns the inventory.
     *
     * @return the {@link HumanEntity} who owns the inventory
     */
    @NotNull HumanEntity getPlayer();

}
