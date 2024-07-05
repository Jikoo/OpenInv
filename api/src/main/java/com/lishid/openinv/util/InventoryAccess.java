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

import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class InventoryAccess implements IInventoryAccess {

    private static Class<?> craftInventory = null;
    private static Method getInventory = null;

    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        try {
            craftInventory = Class.forName(packageName + ".inventory.CraftInventory");
            getInventory = craftInventory.getDeclaredMethod("getInventory");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}
    }

    /**
     * @deprecated use {@link #isUsable()}
     */
    @Deprecated(forRemoval = true)
    public static boolean isUseable() {
        return isUsable();
    }

    public static boolean isUsable() {
        return craftInventory != null && getInventory != null;
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
        return getSpecialInventory(ISpecialPlayerInventory.class, inventory);
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
        return getSpecialInventory(ISpecialEnderChest.class, inventory);
    }

    /**
     * Get a {@link ISpecialInventory} backing an {@link Inventory}. Returns {@code null} if the inventory is not backed
     * by the correct class.
     *
     * @param inventory the Bukkit inventory
     * @return the backing implementation if available
     */
    public static @Nullable ISpecialInventory getInventory(@NotNull Inventory inventory) {
        return getSpecialInventory(ISpecialInventory.class, inventory);
    }

    private static <T extends ISpecialInventory> @Nullable T getSpecialInventory(@NotNull Class<T> expected, @NotNull Inventory inventory) {
        Object inv;
        if (isUsable() && craftInventory.isAssignableFrom(inventory.getClass())) {
            try {
                inv = getInventory.invoke(inventory);
                if (expected.isInstance(inv)) {
                    return expected.cast(inv);
                }
            } catch (ReflectiveOperationException ignored) {}
        }

        // Use reflection to find the IInventory
        inv = ReflectionHelper.grabObjectByType(inventory, expected);

        if (expected.isInstance(inv)) {
            return expected.cast(inv);
        }

        return null;
    }

    /**
     * @deprecated Do not create a new instance to use static methods.
     */
    @Deprecated(forRemoval = true, since = "4.2.0")
    public InventoryAccess() {}

}
