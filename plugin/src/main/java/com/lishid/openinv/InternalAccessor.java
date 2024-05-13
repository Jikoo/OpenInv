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

import com.github.jikoo.planarwrappers.util.version.BukkitVersions;
import com.github.jikoo.planarwrappers.util.version.Version;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.InventoryAccess;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

class InternalAccessor {

    private final @NotNull Plugin plugin;
    private final @Nullable String versionPackage;
    private boolean supported = false;
    private IPlayerDataManager playerDataManager;
    private IAnySilentContainer anySilentContainer;

    InternalAccessor(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.versionPackage = getVersionPackage();
        checkSupported();
    }

    private void checkSupported() {
        if (versionPackage == null) {
            return;
        }
        try {
            Class.forName("com.lishid.openinv.internal." + this.versionPackage + ".SpecialPlayerInventory");
            Class.forName("com.lishid.openinv.internal." + this.versionPackage + ".SpecialEnderChest");
            this.playerDataManager = this.createObject(IPlayerDataManager.class, "PlayerDataManager");
            this.anySilentContainer = this.createObject(IAnySilentContainer.class, "AnySilentContainer");
            this.supported = InventoryAccess.isUsable();
        } catch (Exception ignored) {}
    }

    private @Nullable String getVersionPackage() {
        if (BukkitVersions.MINECRAFT.lessThan(Version.of(1, 20, 3))) { // Min supported version: 1.20.3
            return null;
        }
        if (BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(1, 20, 4))) { // 1.20.3, 1.20.4
            return "v1_20_R3";
        }
        if (BukkitVersions.MINECRAFT.greaterThanOrEqual(Version.of(1, 21))) {
            return null;
        }
        if (BukkitVersions.MINECRAFT.greaterThan(Version.of(1, 20, 6))) {
            return null; // TODO: use at your own risk flag?
        }
        // 1.20.5, 1.20.6
        return "v1_20_R4";
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
        if (BukkitVersions.MINECRAFT.equals(Version.of(1, 20, 3))) { // 1.20.3
            return "https://github.com/Jikoo/OpenInv/releases/tag/4.4.3";
        }
        return "https://github.com/Jikoo/OpenInv/releases";
    }

    private @NotNull <T> T createObject(
            @NotNull Class<? extends T> assignableClass,
            @NotNull String className,
            @NotNull Object @NotNull ... params)
            throws ClassCastException, ReflectiveOperationException {
        // Fetch internal class if it exists.
        Class<?> internalClass = Class.forName("com.lishid.openinv.internal." + this.versionPackage + "." + className);

        // Quick return: no parameters, no need to fiddle about finding the correct constructor.
        if (params.length == 0) {
            return assignableClass.cast(internalClass.getConstructor().newInstance());
        }

        // Search constructors for one matching the given parameters
        nextConstructor: for (Constructor<?> constructor : internalClass.getConstructors()) {
            Class<?>[] requiredClasses = constructor.getParameterTypes();
            if (requiredClasses.length != params.length) {
                continue;
            }
            for (int i = 0; i < params.length; ++i) {
                if (!requiredClasses[i].isAssignableFrom(params[i].getClass())) {
                    continue nextConstructor;
                }
            }
            return assignableClass.cast(constructor.newInstance(params));
        }

        StringBuilder builder = new StringBuilder("Found class ").append(internalClass.getName())
                .append(" but cannot find any matching constructors for [");
        for (Object object : params) {
            builder.append(object.getClass().getName()).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());

        String message = builder.append(']').toString();
        this.plugin.getLogger().warning(message);

        throw new NoSuchMethodException(message);
    }

    private @NotNull <T extends ISpecialInventory> T createSpecialInventory(
            @NotNull Class<? extends T> assignableClass,
            @NotNull String className,
            @NotNull Player player,
            boolean online) throws InstantiationException {
        if (!this.supported) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
        }
        try {
            return this.createObject(assignableClass, className, player, online);
        } catch (Exception original) {
            InstantiationException exception = new InstantiationException(String.format("Unable to create a new %s", className));
            exception.initCause(original.fillInStackTrace());
            throw exception;
        }
    }

    /**
     * Creates an instance of the IAnySilentContainer implementation for the current server version.
     *
     * @return the IAnySilentContainer
     * @throws IllegalStateException if server version is unsupported
     */
    public @NotNull IAnySilentContainer getAnySilentContainer() {
        if (!this.supported) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
        }
        return this.anySilentContainer;
    }

    /**
     * Creates an instance of the IPlayerDataManager implementation for the current server version.
     *
     * @return the IPlayerDataManager
     * @throws IllegalStateException if server version is unsupported
     */
    public @NotNull IPlayerDataManager getPlayerDataManager() {
        if (!this.supported) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
        }
        return this.playerDataManager;
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
        return this.supported;
    }

    /**
     * Creates an instance of the ISpecialEnderChest implementation for the given Player, or
     * null if the current version is unsupported.
     *
     * @param player the Player
     * @param online true if the Player is online
     * @return the ISpecialEnderChest created
     * @throws InstantiationException if the ISpecialEnderChest could not be instantiated
     */
    public ISpecialEnderChest newSpecialEnderChest(final Player player, final boolean online) throws InstantiationException {
        return this.createSpecialInventory(ISpecialEnderChest.class, "SpecialEnderChest", player, online);
    }

    /**
     * Creates an instance of the ISpecialPlayerInventory implementation for the given Player..
     *
     * @param player the Player
     * @param online true if the Player is online
     * @return the ISpecialPlayerInventory created
     * @throws InstantiationException if the ISpecialPlayerInventory could not be instantiated
     */
    public ISpecialPlayerInventory newSpecialPlayerInventory(final Player player, final boolean online) throws InstantiationException {
        return this.createSpecialInventory(ISpecialPlayerInventory.class, "SpecialPlayerInventory", player, online);
    }

}
