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
import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.InventoryAccess;
import java.lang.reflect.Constructor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

class InternalAccessor {

    private final @NotNull Plugin plugin;
    private final String version;
    private boolean supported = false;
    private IPlayerDataManager playerDataManager;
    private IAnySilentContainer anySilentContainer;

    InternalAccessor(@NotNull Plugin plugin) {
        this.plugin = plugin;

        String packageName = plugin.getServer().getClass().getPackage().getName();
        this.version = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            Class.forName("com.lishid.openinv.internal." + this.version + ".SpecialPlayerInventory");
            Class.forName("com.lishid.openinv.internal." + this.version + ".SpecialEnderChest");
            this.playerDataManager = this.createObject(IPlayerDataManager.class, "PlayerDataManager");
            this.anySilentContainer = this.createObject(IAnySilentContainer.class, "AnySilentContainer");
            this.supported = InventoryAccess.isUsable();
        } catch (Exception ignored) {}
    }

    public String getReleasesLink() {

        return switch (version) {
            case "1_4_5", "1_4_6", "v1_4_R1", "v1_5_R2", "v1_5_R3", "v1_6_R1", "v1_6_R2", "v1_6_R3",
                    "v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4", "v1_8_R1", "v1_8_R2",
                    "v1_9_R1", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1"
                    -> "https://github.com/lishid/OpenInv/releases/tag/4.0.0 (OpenInv-legacy)";
            case "v1_13_R1" -> "https://github.com/lishid/OpenInv/releases/tag/4.0.0";
            case "v1_13_R2" -> "https://github.com/lishid/OpenInv/releases/tag/4.0.7";
            case "v1_14_R1" -> "https://github.com/lishid/OpenInv/releases/tag/4.1.1";
            case "v1_16_R1" -> "https://github.com/lishid/OpenInv/releases/tag/4.1.4";
            case "v1_8_R3", "v1_15_R1", "v1_16_R2" -> "https://github.com/lishid/OpenInv/releases/tag/4.1.5";
            case "v1_16_R3" -> "https://github.com/Jikoo/OpenInv/releases/tag/4.1.8";
            case "v1_17_R1", "v1_18_R1" -> "https://github.com/Jikoo/OpenInv/releases/tag/4.1.10";
            case "v1_19_R1" -> "https://github.com/Jikoo/OpenInv/releases/tag/4.2.2";
            case "v1_18_R2", "v1_19_R2" -> "https://github.com/Jikoo/OpenInv/releases/tag/4.3.0";
            default -> "https://github.com/Jikoo/OpenInv/releases";
        };
    }

    private @NotNull <T> T createObject(
            @NotNull Class<? extends T> assignableClass,
            @NotNull String className,
            @NotNull Object @NotNull ... params)
            throws ClassCastException, ReflectiveOperationException {
        // Fetch internal class if it exists.
        Class<?> internalClass = Class.forName("com.lishid.openinv.internal." + this.version + "." + className);

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
            throw new IllegalStateException(String.format("Unsupported server version %s!", this.version));
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
            throw new IllegalStateException(String.format("Unsupported server version %s!", this.version));
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
            throw new IllegalStateException(String.format("Unsupported server version %s!", this.version));
        }
        return this.playerDataManager;
    }

    /**
     * Gets the server implementation version.
     *
     * @return the version
     */
    public @NotNull String getVersion() {
        return this.version;
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
