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

package com.lishid.openinv.util;

import com.github.jikoo.planarwrappers.util.version.BukkitVersions;
import com.github.jikoo.planarwrappers.util.version.Version;
import com.lishid.openinv.internal.Accessor;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.PlayerManager;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class InternalAccessor {

    private @Nullable Accessor internal;

    public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {

        try {
            if (BukkitVersions.MINECRAFT.equals(Version.of(1, 21, 3))) {
                internal = new com.lishid.openinv.internal.v1_21_R2.InternalAccessor(logger, lang);
            } else if (BukkitVersions.MINECRAFT.equals(Version.of(1, 21, 1))) {
                internal = new com.lishid.openinv.internal.v1_21_R1.InternalAccessor(logger, lang);
            }
            if (internal != null) {
                InventoryAccess.setProvider(internal::get);
            }
        } catch (NoClassDefFoundError | Exception e) {
            internal = null;
            InventoryAccess.setProvider(null);
        }
    }

    public String getReleasesLink() {
        if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 4, 4))) { // Good luck.
            return "https://dev.bukkit.org/projects/openinv/files?&sort=datecreated";
        }
        if (BukkitVersions.MINECRAFT.equals(Version.of(1, 8, 8))) { // 1.8.8
            return "https://github.com/lishid/OpenInv/releases/tag/4.1.5";
        }
        if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 13))) { // 1.4.4+ had versioned packages.
            return "https://github.com/lishid/OpenInv/releases/tag/4.0.0 (OpenInv-legacy)";
        }
        if (BukkitVersions.MINECRAFT.equals(Version.of(1, 13))) { // 1.13
            return "https://github.com/lishid/OpenInv/releases/tag/4.0.0";
        }
        if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 14))) { // 1.13.1, 1.13.2
            return "https://github.com/lishid/OpenInv/releases/tag/4.0.7";
        }
        if (BukkitVersions.MINECRAFT.equals(Version.of(1, 14))) { // 1.14 to 1.14.1 had no revision bump.
            return "https://github.com/lishid/OpenInv/releases/tag/4.0.0";
        }
        if (BukkitVersions.MINECRAFT.equals(Version.of(1, 14, 1))) { // 1.14.1 to 1.14.2 had no revision bump.
            return "https://github.com/lishid/OpenInv/releases/tag/4.0.1";
        }
        if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 15))) { // 1.14.2
            return "https://github.com/lishid/OpenInv/releases/tag/4.1.1";
        }
        if (BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(1, 15, 1))) { // 1.15, 1.15.1
            return "https://github.com/lishid/OpenInv/releases/tag/4.1.5";
        }
        if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 16))) { // 1.15.2
            return "https://github.com/Jikoo/OpenInv/commit/502f661be39ee85d300851dd571f3da226f12345 (never released)";
        }
        if (BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(1, 16, 1))) { // 1.16, 1.16.1
            return "https://github.com/lishid/OpenInv/releases/tag/4.1.4";
        }
        if (BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(1, 16, 3))) { // 1.16.2, 1.16.3
            return "https://github.com/lishid/OpenInv/releases/tag/4.1.5";
        }
        if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 17))) { // 1.16.4, 1.16.5
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.1.8";
        }
        if (BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(1, 18, 1))) { // 1.17, 1.18, 1.18.1
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.1.10";
        }
        if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 19))) { // 1.18.2
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.3.0";
        }
        if (BukkitVersions.MINECRAFT.equals(Version.of(1, 19))) { // 1.19
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.2.0";
        }
        if (BukkitVersions.MINECRAFT.equals(Version.of(1, 19, 1))) { // 1.19.1
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.2.2";
        }
        if (BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(1, 19, 3))) { // 1.19.2, 1.19.3
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.3.0";
        }
        if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 20))) { // 1.19.4
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.4.3";
        }
        if (BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(1, 20, 1))) { // 1.20, 1.20.1
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.4.1";
        }
        if (BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(1, 20, 3))) { // 1.20.2, 1.20.3
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.4.3";
        }
        if (BukkitVersions.MINECRAFT.equals(Version.of(1, 20, 5))) { // 1.20.5
            return "Unsupported; upgrade to 1.20.6: https://github.com/Jikoo/OpenInv/releases/tag/5.1.2";
        }
        if (BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(1, 21))) { // 1.20.4, 1.20.6, 1.21
            return "https://github.com/Jikoo/OpenInv/releases/tag/5.1.2";
        }
        return "https://github.com/Jikoo/OpenInv/releases";
    }

    /**
     * Reload internal features.
     */
    public void reload(ConfigurationSection config) {
        if (internal != null) {
            internal.reload(config);
        }
    }

    /**
     * Gets the server implementation version.
     *
     * @return the version
     */
    public @NotNull String getVersion() {
        return BukkitVersions.MINECRAFT.toString();
    }

    /**
     * Checks if the server implementation is supported.
     *
     * @return true if initialized for a supported server version
     */
    public boolean isSupported() {
        return internal != null;
    }

    /**
     * Get the instance of the IAnySilentContainer implementation for the current server version.
     *
     * @return the IAnySilentContainer
     * @throws IllegalStateException if server version is unsupported
     */
    public @NotNull IAnySilentContainer getAnySilentContainer() {
        if (internal == null) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
        }
        return internal.getAnySilentContainer();
    }

    public @Nullable InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory, boolean viewOnly) {
        if (internal == null) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
        }
        return internal.getPlayerManager().openInventory(player, inventory, viewOnly);
    }

    /**
     * Get the instance of the IPlayerDataManager implementation for the current server version.
     *
     * @return the IPlayerDataManager
     * @throws IllegalStateException if server version is unsupported
     */
    @NotNull PlayerManager getPlayerDataManager() {
        if (internal == null) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
        }
        return internal.getPlayerManager();
    }

    /**
     * Creates an instance of the ISpecialEnderChest implementation for the given Player.
     *
     * @param player the Player
     * @return the ISpecialEnderChest created
     */
    ISpecialEnderChest createEnderChest(final Player player) {
        if (internal == null) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
        }
        return internal.createEnderChest(player);
    }

    /**
     * Creates an instance of the ISpecialPlayerInventory implementation for the given Player.
     *
     * @param player the Player
     * @return the ISpecialPlayerInventory created
     */
    ISpecialPlayerInventory createInventory(final Player player) {
        if (internal == null) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
        }
        return internal.createPlayerInventory(player);
    }

}
