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

import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Interface defining behavior for the OpenInv plugin.
 */
public interface IOpenInv {

    /**
     * Check if the server version is supported by OpenInv.
     *
     * @return true if the server version is supported
     */
    boolean isSupportedVersion();

    /**
     * Check the configuration value for whether OpenInv saves player data when unloading players. This is exclusively
     * for users who do not allow editing of inventories, only viewing, and wish to prevent any possibility of bugs such
     * as lishid#40. If true, OpenInv will not ever save any edits made to players.
     *
     * @return false unless configured otherwise
     */
    boolean disableSaving();

    /**
     * Check the configuration value for whether OpenInv allows offline access. If true, OpenInv will not load or allow
     * modification of players while they are not online. This does not prevent other plugins from using existing loaded
     * players who have gone offline.
     *
     * @return false unless configured otherwise
     * @since 4.2.0
     */
    boolean disableOfflineAccess();

    /**
     * Check the configuration value for whether OpenInv uses history for opening commands. If false, OpenInv will use
     * the previous parameterized search when no parameters are provided.
     *
     * @return false unless configured otherwise
     * @since 4.3.0
     */
    boolean noArgsOpensSelf();

    /**
     * Get the active {@link IAnySilentContainer} implementation.
     *
     * @return the active implementation for the server version
     * @throws IllegalStateException if the server version is unsupported
     */
    @NotNull IAnySilentContainer getAnySilentContainer();

    /**
     * Get whether a user has AnyContainer mode enabled.
     *
     * @param offline the user to obtain the state of
     * @return true if AnyContainer mode is enabled
     */
    boolean getAnyContainerStatus(@NotNull OfflinePlayer offline);

    /**
     * Set whether a user has AnyContainer mode enabled.
     *
     * @param offline the user to set the state of
     * @param status the state of the mode
     */
    void setAnyContainerStatus(@NotNull OfflinePlayer offline, boolean status);

    /**
     * Get whether a user has SilentContainer mode enabled.
     *
     * @param offline the user to obtain the state of
     * @return true if SilentContainer mode is enabled
     */
    boolean getSilentContainerStatus(@NotNull OfflinePlayer offline);

    /**
     * Set whether a user has SilentContainer mode enabled.
     *
     * @param offline the user to set the state of
     * @param status the state of the mode
     */
    void setSilentContainerStatus(@NotNull OfflinePlayer offline, boolean status);

    /**
     * Get an {@link ISpecialEnderChest} for a user.
     *
     * @param player the {@link Player} owning the inventory
     * @param online whether the owner is currently online
     * @return the created inventory
     * @throws IllegalStateException if the server version is unsupported
     * @throws InstantiationException if there was an issue creating the inventory
     */
    @NotNull ISpecialEnderChest getSpecialEnderChest(@NotNull Player player, boolean online) throws InstantiationException;

    /**
     * Get an {@link ISpecialPlayerInventory} for a user.
     *
     * @param player the {@link Player} owning the inventory
     * @param online whether the owner is currently online
     * @return the created inventory
     * @throws IllegalStateException if the server version is unsupported
     * @throws InstantiationException if there was an issue creating the inventory
     */
    @NotNull ISpecialPlayerInventory getSpecialInventory(@NotNull Player player, boolean online) throws InstantiationException;

    /**
     * Open an {@link ISpecialInventory} for a {@link Player}.
     *
     * @param player the viewer
     * @param inventory the inventory to open
     * @return the resulting {@link InventoryView}
     */
    @Nullable InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory);

    /**
     * Check if a {@link Player} is currently loaded by OpenInv.
     *
     * @param playerUuid the {@link UUID} of the {@code Player}
     * @return whether the {@code Player} is loaded
     * @since 4.2.0
     */
    boolean isPlayerLoaded(@NotNull UUID playerUuid);

    /**
     * Load a {@link Player} from an {@link OfflinePlayer}. If the user has not played before or the default world for
     * the server is not loaded, this will return {@code null}.
     *
     * @param offline the {@code OfflinePlayer} to load a {@code Player} for
     * @return the loaded {@code Player}
     * @throws IllegalStateException if the server version is unsupported
     */
    @Nullable Player loadPlayer(@NotNull final OfflinePlayer offline);

    /**
     * Match an existing {@link OfflinePlayer}. If the name is a {@link UUID#toString() UUID string}, this will only
     * return the user if they have actually played on the server before, unlike {@link Bukkit#getOfflinePlayer(UUID)}.
     *
     * <p>This method is potentially very heavily blocking. It should not ever be called on the
     * main thread, and if it is, a stack trace will be displayed alerting server owners to the
     * call.
     *
     * @param name the string to match
     * @return the user with the closest matching name
     */
    @Nullable OfflinePlayer matchPlayer(@NotNull String name);

    /**
     * Forcibly close inventories of and unload any cached data for a user.
     *
     * @param offline the {@link OfflinePlayer} to unload
     */
    void unload(@NotNull OfflinePlayer offline);

    Logger getLogger();

}
