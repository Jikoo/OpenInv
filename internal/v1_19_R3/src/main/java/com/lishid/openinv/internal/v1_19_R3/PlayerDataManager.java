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

package com.lishid.openinv.internal.v1_19_R3;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.OpenInventoryView;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.logging.Logger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftContainer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerDataManager implements IPlayerDataManager {

    private @Nullable Field bukkitEntity;

    public PlayerDataManager() {
        try {
            bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
        } catch (NoSuchFieldException e) {
            Logger logger = OpenInv.getPlugin(OpenInv.class).getLogger();
            logger.warning("Unable to obtain field to inject custom save process - players' mounts may be deleted when loaded.");
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
            throw new RuntimeException("Unable to fetch EntityPlayer from provided Player implementation");
        }

        return nmsPlayer;
    }

    @Override
    public @Nullable Player loadPlayer(@NotNull final OfflinePlayer offline) {
        // Ensure player has data
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        // Create a profile and entity to load the player data
        // See net.minecraft.server.players.PlayerList#canPlayerLogin
        // and net.minecraft.server.network.ServerLoginPacketListenerImpl#handleHello
        GameProfile profile = new GameProfile(offline.getUniqueId(),
                offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel worldServer = server.getLevel(Level.OVERWORLD);

        if (worldServer == null) {
            return null;
        }

        ServerPlayer entity = new ServerPlayer(server, worldServer, profile);

        // Stop listening for advancement progression - if this is not cleaned up, loading causes a memory leak.
        entity.getAdvancements().stopListening();

        try {
            injectPlayer(entity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Load data. This also reads basic data into the player.
        // See CraftPlayer#loadData
        CompoundTag loadedData = server.getPlayerList().playerIo.load(entity);

        if (loadedData == null) {
            // Exceptions with loading are logged by Mojang.
            return null;
        }

        // Also read "extra" data.
        entity.readAdditionalSaveData(loadedData);

        if (entity.level == null) {
            // Paper: Move player to spawn
            // SPIGOT-7340: Cannot call ServerPlayer#spawnIn with a null world
            ServerLevel level = null;
            Vec3 position = null;
            if (entity.getRespawnDimension() != null) {
                level = entity.server.getLevel(entity.getRespawnDimension());
                if (level != null && entity.getRespawnPosition() != null) {
                    position = net.minecraft.world.entity.player.Player.findRespawnPositionAndUseSpawnBlock(level, entity.getRespawnPosition(), entity.getRespawnAngle(), false, false).orElse(null);
                }
            }
            if (level == null || position == null) {
                level = ((CraftWorld) server.server.getWorlds().get(0)).getHandle();
                position = Vec3.atCenterOf(level.getSharedSpawnPos());
            }
            entity.level = level;
            entity.setPos(position);
        }

        // Return the Bukkit entity.
        return entity.getBukkitEntity();
    }

    void injectPlayer(ServerPlayer player) throws IllegalAccessException {
        if (bukkitEntity == null) {
            return;
        }

        bukkitEntity.setAccessible(true);

        bukkitEntity.set(player, new OpenPlayer(player.server.server, player));
    }

    @NotNull
    @Override
    public Player inject(@NotNull Player player) {
        try {
            ServerPlayer nmsPlayer = getHandle(player);
            if (nmsPlayer.getBukkitEntity() instanceof OpenPlayer openPlayer) {
                return openPlayer;
            }
            injectPlayer(nmsPlayer);
            return nmsPlayer.getBukkitEntity();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return player;
        }
    }

    @Nullable
    @Override
    public InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory) {

        ServerPlayer nmsPlayer = getHandle(player);

        if (nmsPlayer.connection == null) {
            return null;
        }

        InventoryView view = getView(player, inventory);

        if (view == null) {
            return player.openInventory(inventory.getBukkitInventory());
        }

        AbstractContainerMenu container = new CraftContainer(view, nmsPlayer, nmsPlayer.nextContainerCounter()) {
            @Override
            public MenuType<?> getType() {
                return getContainers(inventory.getBukkitInventory().getSize());
            }
        };

        container.setTitle(Component.literal(view.getTitle()));
        container = CraftEventFactory.callInventoryOpenEvent(nmsPlayer, container);

        if (container == null) {
            return null;
        }

        nmsPlayer.connection.send(new ClientboundOpenScreenPacket(container.containerId, container.getType(),
                Component.literal(container.getBukkitView().getTitle())));
        nmsPlayer.containerMenu = container;
        nmsPlayer.initMenu(container);

        return container.getBukkitView();

    }

    private @Nullable InventoryView getView(Player player, ISpecialInventory inventory) {
        if (inventory instanceof SpecialEnderChest) {
            return new OpenInventoryView(player, inventory, "container.enderchest", "'s Ender Chest");
        } else if (inventory instanceof SpecialPlayerInventory) {
            return new OpenInventoryView(player, inventory, "container.player", "'s Inventory");
        } else {
            return null;
        }
    }

    static @NotNull MenuType<?> getContainers(int inventorySize) {

        return switch (inventorySize) {
            case 9 -> MenuType.GENERIC_9x1;
            case 18 -> MenuType.GENERIC_9x2;
            case 36 -> MenuType.GENERIC_9x4; // PLAYER
            case 41, 45 -> MenuType.GENERIC_9x5;
            case 54 -> MenuType.GENERIC_9x6;
            default -> MenuType.GENERIC_9x3; // Default 27-slot inventory
        };
    }

    @Override
    public int convertToPlayerSlot(InventoryView view, int rawSlot) {
        int topSize = view.getTopInventory().getSize();
        if (topSize <= rawSlot) {
            // Slot is not inside special inventory, use Bukkit logic.
            return view.convertSlot(rawSlot);
        }

        // Main inventory, slots 0-26 -> 9-35
        if (rawSlot < 27) {
            return rawSlot + 9;
        }
        // Hotbar, slots 27-35 -> 0-8
        if (rawSlot < 36) {
            return rawSlot - 27;
        }
        // Armor, slots 36-39 -> 39-36
        if (rawSlot < 40) {
            return 36 + (39 - rawSlot);
        }
        // Off hand
        if (rawSlot == 40) {
            return 40;
        }
        // Drop slots, "out of inventory"
        return -1;
    }

}
