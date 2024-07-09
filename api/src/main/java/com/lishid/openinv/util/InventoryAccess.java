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

package com.lishid.openinv.util;

import com.google.errorprone.annotations.RestrictedApi;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public final class InventoryAccess {

    private static @Nullable BiFunction<Inventory, Class<? extends ISpecialInventory>, ISpecialInventory> provider;

    public static boolean isUsable() {
        return provider != null;
    }

    /**
     * Check if an {@link Inventory} is an {@link ISpecialPlayerInventory} implementation.
     *
     * @param inventory the Bukkit inventory
     * @return true if backed by the correct implementation
     */
    public static boolean isPlayerInventory(@NotNull Inventory inventory) {
        return getPlayerInventory(inventory) != null;
    }

    /**
     * Get the {@link ISpecialPlayerInventory} backing an {@link Inventory}. Returns {@code null} if the inventory is
     * not backed by the correct class.
     *
     * @param inventory the Bukkit inventory
     * @return the backing implementation if available
     */
    public static @Nullable ISpecialPlayerInventory getPlayerInventory(@NotNull Inventory inventory) {
        return provider == null ? null : (ISpecialPlayerInventory) provider.apply(inventory, ISpecialPlayerInventory.class);
    }

    /**
     * Check if an {@link Inventory} is an {@link ISpecialEnderChest} implementation.
     *
     * @param inventory the Bukkit inventory
     * @return true if backed by the correct implementation
     */
    public static boolean isEnderChest(@NotNull Inventory inventory) {
        return getEnderChest(inventory) != null;
    }

    /**
     * Get the {@link ISpecialEnderChest} backing an {@link Inventory}. Returns {@code null} if the inventory is
     * not backed by the correct class.
     *
     * @param inventory the Bukkit inventory
     * @return the backing implementation if available
     */
    public static @Nullable ISpecialEnderChest getEnderChest(@NotNull Inventory inventory) {
        return provider == null ? null : (ISpecialEnderChest) provider.apply(inventory, ISpecialEnderChest.class);
    }

    /**
     * Get a {@link ISpecialInventory} backing an {@link Inventory}. Returns {@code null} if the inventory is not backed
     * by the correct class.
     *
     * @param inventory the Bukkit inventory
     * @return the backing implementation if available
     */
    public static @Nullable ISpecialInventory getInventory(@NotNull Inventory inventory) {
        return provider == null ? null : provider.apply(inventory, ISpecialInventory.class);
    }

    @RestrictedApi(
        explanation = "Not part of the API.",
        link = "",
        allowedOnPath = ".*/com/lishid/openinv/util/InternalAccessor.java")
    @ApiStatus.Internal
    static void setProvider(@Nullable BiFunction<Inventory, Class<? extends ISpecialInventory>, ISpecialInventory> provider) {
        InventoryAccess.provider = provider;
    }

    private InventoryAccess() {
        throw new IllegalStateException("Cannot create instance of utility class.");
    }

}
