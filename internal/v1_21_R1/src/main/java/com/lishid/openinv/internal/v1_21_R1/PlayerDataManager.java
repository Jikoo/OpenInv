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

package com.lishid.openinv.internal.v1_21_R1;

import com.github.jikoo.planarwrappers.function.TriFunction;
import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.InventoryViewTitle;
import com.lishid.openinv.internal.OpenInventoryView;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftContainer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayerDataManager implements IPlayerDataManager {

    private static boolean paper;
    private static TriFunction<Player, ISpecialInventory, String, InventoryView> viewProvider;

    static {
        try {
            Class.forName("io.papermc.paper.configuration.Configuration");
            paper = true;
        } catch (ClassNotFoundException ignored) {
            paper = false;
        }
        try {
            Class.forName(Bukkit.getServer().getClass().getPackageName() + ".inventory.CraftAbstractInventoryView");
            viewProvider = OpenCraftView::new;
        } catch (ClassNotFoundException ignored) {
            viewProvider = OpenInventoryView::new;
        }
    }

    private @Nullable Field bukkitEntity;

    public PlayerDataManager() {
        try {
            bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
        } catch (NoSuchFieldException e) {
            Logger logger = JavaPlugin.getPlugin(OpenInv.class).getLogger();
            logger.warning("Unable to obtain field to inject custom save process - certain player data may be lost when saving!");
            logger.log(java.util.logging.Level.WARNING, e.getMessage(), e);
            bukkitEntity = null;
        }
    }

    public static @NotNull ServerPlayer getHandle(final Player player) {
        if (player instanceof CraftPlayer) {
            return ((CraftPlayer) player).getHandle();
        }

        Server server = player.getServer();
        ServerPlayer nmsPlayer = null;

        if (server instanceof CraftServer) {
            nmsPlayer = ((CraftServer) server).getHandle().getPlayer(player.getUniqueId());
        }

        if (nmsPlayer == null) {
            // Could use reflection to examine fields, but it's honestly not worth the bother.
            throw new RuntimeException("Unable to fetch EntityPlayer from Player implementation " + player.getClass().getName());
        }

        return nmsPlayer;
    }

    @Override
    public @Nullable Player loadPlayer(@NotNull final OfflinePlayer offline) {
        // Ensure player has data
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel worldServer = server.getLevel(Level.OVERWORLD);

        if (worldServer == null) {
            return null;
        }

        // Create a new ServerPlayer.
        ServerPlayer entity = createNewPlayer(server, worldServer, offline);

        // Stop listening for advancement progression - if this is not cleaned up, loading causes a memory leak.
        entity.getAdvancements().stopListening();

        // Try to load the player's data.
        if (loadData(entity)) {
            // If data is loaded successfully, return the Bukkit entity.
            return entity.getBukkitEntity();
        }

        return null;
    }

    private @NotNull ServerPlayer createNewPlayer(
        @NotNull MinecraftServer server,
        @NotNull ServerLevel worldServer,
        @NotNull final OfflinePlayer offline) {
        // See net.minecraft.server.players.PlayerList#canPlayerLogin(ServerLoginPacketListenerImpl, GameProfile)
        // See net.minecraft.server.network.ServerLoginPacketListenerImpl#handleHello(ServerboundHelloPacket)
        GameProfile profile = new GameProfile(offline.getUniqueId(),
            offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());

        ClientInformation dummyInfo = new ClientInformation(
            "en_us",
            1, // Reduce distance just in case.
            ChatVisiblity.HIDDEN, // Don't accept chat.
            false,
            ServerPlayer.DEFAULT_MODEL_CUSTOMIZATION,
            ServerPlayer.DEFAULT_MAIN_HAND,
            true,
            false // Don't list in player list (not that this player is in the list anyway).
        );

        ServerPlayer entity = new ServerPlayer(server, worldServer, profile, dummyInfo);

        try {
            injectPlayer(entity);
        } catch (IllegalAccessException e) {
            JavaPlugin.getPlugin(OpenInv.class).getLogger().log(
                java.util.logging.Level.WARNING,
                e,
                () -> "Unable to inject ServerPlayer, certain player data may be lost when saving!");
        }

        return entity;
    }

    static boolean loadData(@NotNull ServerPlayer player) {
        // See CraftPlayer#loadData
        CompoundTag loadedData = player.server.getPlayerList().playerIo.load(player).orElse(null);

        if (loadedData == null) {
            // Exceptions with loading are logged by Mojang.
            return false;
        }

        // Read basic data into the player.
        player.load(loadedData);
        // Also read "extra" data.
        player.readAdditionalSaveData(loadedData);
        // Game type settings are also loaded separately.
        player.loadGameTypes(loadedData);

        if (paper) {
            // Paper: world is not loaded by ServerPlayer#load(CompoundTag).
            parseWorld(player, loadedData);
        }

        return true;
    }

    private static void parseWorld(@NotNull ServerPlayer player, @NotNull CompoundTag loadedData) {
        // See PlayerList#placeNewPlayer
        World bukkitWorld;
        if (loadedData.contains("WorldUUIDMost") && loadedData.contains("WorldUUIDLeast")) {
            // Modern Bukkit world.
            bukkitWorld = Bukkit.getServer().getWorld(new UUID(loadedData.getLong("WorldUUIDMost"), loadedData.getLong("WorldUUIDLeast")));
        } else if (loadedData.contains("world", net.minecraft.nbt.Tag.TAG_STRING)) {
            // Legacy Bukkit world.
            bukkitWorld = Bukkit.getServer().getWorld(loadedData.getString("world"));
        } else {
            // Vanilla player data.
            DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, loadedData.get("Dimension")))
                .resultOrPartial(JavaPlugin.getPlugin(OpenInv.class).getLogger()::warning)
                .map(player.server::getLevel)
                // If ServerLevel exists, set, otherwise move to spawn.
                .ifPresentOrElse(player::setServerLevel, () -> player.spawnIn(null));
            return;
        }
        if (bukkitWorld == null) {
            player.spawnIn(null);
            return;
        }
        player.setServerLevel(((CraftWorld) bukkitWorld).getHandle());
    }

    private void injectPlayer(ServerPlayer player) throws IllegalAccessException {
        if (bukkitEntity == null) {
            return;
        }

        bukkitEntity.setAccessible(true);

        bukkitEntity.set(player, new OpenPlayer(player.server.server, player));
    }

    @Override
    public @NotNull Player inject(@NotNull Player player) {
        try {
            ServerPlayer nmsPlayer = getHandle(player);
            if (nmsPlayer.getBukkitEntity() instanceof OpenPlayer openPlayer) {
                return openPlayer;
            }
            injectPlayer(nmsPlayer);
            return nmsPlayer.getBukkitEntity();
        } catch (IllegalAccessException e) {
            JavaPlugin.getPlugin(OpenInv.class).getLogger().log(
                java.util.logging.Level.WARNING,
                e,
                () -> "Unable to inject ServerPlayer, certain player data may be lost when saving!");
            return player;
        }
    }

    @Override
    public @Nullable InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory) {
        if (inventory instanceof ISpecialPlayerInventory) {
            return player.openInventory(inventory.getBukkitInventory());
        }

        ServerPlayer nmsPlayer = getHandle(player);

        if (nmsPlayer.connection == null) {
            return null;
        }

        InventoryViewTitle viewTitle = InventoryViewTitle.of(inventory);

        if (viewTitle == null) {
            return player.openInventory(inventory.getBukkitInventory());
        }

        String originalTitle = viewTitle.getTitle(player, inventory);
        InventoryView view = viewProvider.apply(player, inventory, originalTitle);
        Component title = Component.literal(originalTitle);
        AbstractContainerMenu container = new CraftContainer(view, nmsPlayer, nmsPlayer.nextContainerCounter()) {
            @Override
            public MenuType<?> getType() {
                return getContainers(inventory.getBukkitInventory().getSize());
            }
        };
        container.setTitle(title);

        container = CraftEventFactory.callInventoryOpenEvent(nmsPlayer, container);

        if (container == null) {
            return null;
        }

        // Note: Reusing component prevents plugins from changing title during InventoryOpenEvent, but there's not much
        // we can do about that - we can't call InventoryView#getTitle on older versions without causing an
        // IncompatibleClassChangeError due to the fact that InventoryView is now an interface.
        nmsPlayer.connection.send(new ClientboundOpenScreenPacket(container.containerId, container.getType(), title));
        nmsPlayer.containerMenu = container;
        nmsPlayer.initMenu(container);

        return container.getBukkitView();

    }

    static @NotNull MenuType<?> getContainers(int inventorySize) {
        return switch (inventorySize) {
            case 9 -> MenuType.GENERIC_9x1;
            case 18 -> MenuType.GENERIC_9x2;
            case 36 -> MenuType.GENERIC_9x4;
            case 41, 45 -> MenuType.GENERIC_9x5;
            case 54 -> MenuType.GENERIC_9x6;
            default -> MenuType.GENERIC_9x3; // Default 27-slot inventory
        };
    }

}
