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

import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
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
     * @param player the player opening the container
     * @param block  the {@link Block} of the container
     * @return true if the container is blocked
     * @deprecated use {@link #isAnyContainerNeeded(Block)}
     */
    @Deprecated(forRemoval = true, since = "4.1.9")
    default boolean isAnyContainerNeeded(@NotNull Player player, @NotNull Block block) {
        return isAnyContainerNeeded(block);
    }

    /**
     * Check if the container at the given coordinates is blocked.
     *
     * @param block the {@link Block} of the container
     * @return true if the container is blocked
     */
    default boolean isAnyContainerNeeded(@NotNull Block block) {
        BlockState blockState = block.getState();

        // Barrels do not require AnyContainer.
        if (blockState instanceof Barrel) {
            return false;
        }

        // Enderchests require a non-occluding block on top to open.
        if (blockState instanceof EnderChest) {
            return block.getRelative(0, 1, 0).getType().isOccluding();
        }

        // Shulker boxes require 1/2 a block clear in the direction they open.
        if (blockState instanceof ShulkerBox shulker) {
            if (isShulkerBlocked(shulker)) {
                return true;
            }
        }

        if (!(blockState instanceof org.bukkit.block.Chest)) {
            return false;
        }

        if (isChestBlocked(block)) {
            return true;
        }

        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Chest chest) || ((Chest) blockData).getType() == Chest.Type.SINGLE) {
            return false;
        }

        int ordinal = (chest.getFacing().ordinal() + 4 + (chest.getType() == Chest.Type.RIGHT ? -1 : 1)) % 4;
        BlockFace relativeFace = BlockFace.values()[ordinal];
        org.bukkit.block.Block relative = block.getRelative(relativeFace);

        if (relative.getType() != block.getType()) {
            return false;
        }

        BlockData relativeData = relative.getBlockData();
        if (!(relativeData instanceof Chest relativeChest)) {
            return false;
        }

        if (relativeChest.getFacing() != chest.getFacing()
                || relativeChest.getType() != (chest.getType() == Chest.Type.RIGHT ? Chest.Type.LEFT : Chest.Type.RIGHT)) {
            return false;
        }

        return isChestBlocked(relative);
    }

    /**
     * Check if a {@link ShulkerBox} cannot be opened under ordinary circumstances.
     *
     * @param shulkerBox the shulker box container
     * @return whether the container is blocked
     */
    boolean isShulkerBlocked(@NotNull ShulkerBox shulkerBox);

    /**
     * Check if a chest cannot be opened under ordinary circumstances.
     *
     * @param chest the chest block
     * @return whether the container is blocked
     */
    default boolean isChestBlocked(@NotNull Block chest) {
        org.bukkit.block.Block relative = chest.getRelative(0, 1, 0);
        return relative.getType().isOccluding()
                || chest.getWorld().getNearbyEntities(BoundingBox.of(relative), entity -> entity instanceof Cat).size() > 0;
    }

    /**
     * Check if the given {@link Block} is a container which can be unblocked or silenced.
     *
     * @param block the potential container
     * @return true if the type is a supported container
     */
    default boolean isAnySilentContainer(@NotNull Block block) {
        return isAnySilentContainer(block.getState());
    }

    /**
     * Check if the given {@link BlockState} is a container which can be unblocked or silenced.
     *
     * @param blockState the potential container
     * @return true if the type is a supported container
     */
    default boolean isAnySilentContainer(@NotNull BlockState blockState) {
        return blockState instanceof InventoryHolder holder && isAnySilentContainer(holder);
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
