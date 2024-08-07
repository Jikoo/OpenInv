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

package com.lishid.openinv.internal;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public interface IAnySilentContainer {

    /**
     * Forcibly open the container at the given coordinates for the Player. This will open blocked containers! Be sure
     * to check {@link #isAnyContainerNeeded(Block)} first if that is not desirable.
     *
     * @param player the {@link Player} opening the container
     * @param silent whether the container's noise is to be silenced
     * @param block  the {@link Block} of the container
     * @return true if the container can be opened
     */
    boolean activateContainer(@NotNull Player player, boolean silent, @NotNull Block block);

    /**
     * Perform operations required to close the current container silently.
     *
     * @param player the {@link Player} closing a container
     */
    void deactivateContainer(@NotNull Player player);

    /**
     * Check if the container at the given coordinates is blocked.
     *
     * @param block the {@link Block} of the container
     * @return true if the container is blocked
     */
    boolean isAnyContainerNeeded(@NotNull Block block);

    /**
     * Check if a shulker box block cannot be opened under ordinary circumstances.
     *
     * @param shulkerBox the shulker box block
     * @return whether the container is blocked
     */
    default boolean isShulkerBlocked(@NotNull Block shulkerBox) {
        Directional directional = (Directional) shulkerBox.getBlockData();
        BlockFace facing = directional.getFacing();
        // Construct a new 1-block bounding box at the origin.
        BoundingBox box = new BoundingBox(0, 0, 0, 1, 1, 1);
        // Expand the box in the direction the shulker will open.
        box.expand(facing, 0.5);
        // Move the box away from the origin by a block so only the expansion intersects with a box around the origin.
        box.shift(facing.getOppositeFace().getDirection());
        // Check if the relative block's collision shape (which will be at the origin) intersects with the expanded box.
        return shulkerBox.getRelative(facing).getCollisionShape().overlaps(box);
    }

    /**
     * Check if a chest cannot be opened under ordinary circumstances.
     *
     * @param chest the chest block
     * @return whether the container is blocked
     */
    default boolean isChestBlocked(@NotNull Block chest) {
        org.bukkit.block.Block relative = chest.getRelative(0, 1, 0);
        return relative.getType().isOccluding()
                || !chest.getWorld().getNearbyEntities(BoundingBox.of(relative), Cat.class::isInstance).isEmpty();
    }

    /**
     * Check if the given {@link Block} is a container which can be unblocked or silenced.
     *
     * @param block the potential container
     * @return true if the type is a supported container
     */
    boolean isAnySilentContainer(@NotNull Block block);

    /**
     * Check if the given {@link BlockState} is a container which can be unblocked or silenced.
     *
     * @param blockState the potential container
     * @return true if the type is a supported container
     */
    default boolean isAnySilentContainer(@NotNull BlockState blockState) {
        return (blockState instanceof InventoryHolder holder && isAnySilentContainer(holder))
                || blockState instanceof EnderChest;
    }

    /**
     * Check if the given {@link InventoryHolder} is a container which can be unblocked or silenced.
     *
     * @param holder the potential container
     * @return true if the type is a supported container
     */
    default boolean isAnySilentContainer(@NotNull InventoryHolder holder) {
        return holder instanceof org.bukkit.block.EnderChest
                || holder instanceof org.bukkit.block.Chest
                || holder instanceof org.bukkit.block.DoubleChest
                || holder instanceof org.bukkit.block.ShulkerBox
                || holder instanceof org.bukkit.block.Barrel;
    }

}
