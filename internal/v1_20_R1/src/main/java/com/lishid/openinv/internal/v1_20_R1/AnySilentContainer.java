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

package com.lishid.openinv.internal.v1_20_R1;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.util.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.logging.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnySilentContainer implements IAnySilentContainer {

    private @Nullable Field serverPlayerGameModeGameType;

    public AnySilentContainer() {
        try {
            try {
                this.serverPlayerGameModeGameType = ServerPlayerGameMode.class.getDeclaredField("b");
                this.serverPlayerGameModeGameType.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Logger logger = OpenInv.getPlugin(OpenInv.class).getLogger();
                logger.warning("ServerPlayerGameMode#gameModeForPlayer's obfuscated name has changed!");
                logger.warning("Please report this at https://github.com/Jikoo/OpenInv/issues");
                logger.warning("Attempting to fall through using reflection. Please verify that SilentContainer does not fail.");
                // N.B. gameModeForPlayer is (for now) declared before previousGameModeForPlayer so silent shouldn't break.
                this.serverPlayerGameModeGameType = ReflectionHelper.grabFieldByType(ServerPlayerGameMode.class, GameType.class);
            }
        } catch (SecurityException e) {
            Logger logger = OpenInv.getPlugin(OpenInv.class).getLogger();
            logger.warning("Unable to directly write player game mode! SilentContainer will fail.");
            logger.log(java.util.logging.Level.WARNING, "Error obtaining GameType field", e);
        }
    }

    @Override
    public boolean isShulkerBlocked(@NotNull ShulkerBox shulkerBox) {
        org.bukkit.World bukkitWorld = shulkerBox.getWorld();
        if (!(bukkitWorld instanceof CraftWorld)) {
            bukkitWorld = Bukkit.getWorld(bukkitWorld.getUID());
        }

        if (!(bukkitWorld instanceof CraftWorld craftWorld)) {
            Exception exception = new IllegalStateException("AnySilentContainer access attempted on an unknown world!");
            OpenInv.getPlugin(OpenInv.class).getLogger().log(java.util.logging.Level.WARNING, exception.getMessage(), exception);
            return false;
        }

        final ServerLevel world = craftWorld.getHandle();
        final BlockPos blockPosition = new BlockPos(shulkerBox.getX(), shulkerBox.getY(), shulkerBox.getZ());
        final BlockEntity tile = world.getBlockEntity(blockPosition);

        if (!(tile instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity)
                || shulkerBoxBlockEntity.getAnimationStatus() != ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
            return false;
        }

        BlockState blockState = world.getBlockState(blockPosition);

        // See net.minecraft.world.level.block.ShulkerBoxBlock#canOpen
        AABB boundingBox = Shulker.getProgressDeltaAabb(blockState.getValue(ShulkerBoxBlock.FACING), 0.0F, 0.5F)
                .move(blockPosition)
                .deflate(1.0E-6D);
        return !world.noCollision(boundingBox);
    }

    @Override
    public boolean activateContainer(
            @NotNull final Player bukkitPlayer,
            final boolean silentchest,
            @NotNull final org.bukkit.block.Block bukkitBlock) {

        // Silent ender chest is API-only
        if (silentchest && bukkitBlock.getType() == Material.ENDER_CHEST) {
            bukkitPlayer.openInventory(bukkitPlayer.getEnderChest());
            bukkitPlayer.incrementStatistic(Statistic.ENDERCHEST_OPENED);
            return true;
        }

        ServerPlayer player = PlayerDataManager.getHandle(bukkitPlayer);

        final net.minecraft.world.level.Level level = player.level();
        final BlockPos blockPos = new BlockPos(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        final BlockEntity blockEntity = level.getBlockEntity(blockPos);

        if (blockEntity == null) {
            return false;
        }

        if (blockEntity instanceof EnderChestBlockEntity enderChestTile) {
            // Anychest ender chest. See net.minecraft.world.level.block.EnderChestBlock
            PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
            enderChest.setActiveChest(enderChestTile);
            player.openMenu(new SimpleMenuProvider((containerCounter, playerInventory, ignored) -> {
                MenuType<?> containers = PlayerDataManager.getContainers(enderChest.getContainerSize());
                int rows = enderChest.getContainerSize() / 9;
                return new ChestMenu(containers, containerCounter, playerInventory, enderChest, rows);
            }, Component.translatable(("container.enderchest"))));
            bukkitPlayer.incrementStatistic(Statistic.ENDERCHEST_OPENED);
            return true;
        }

        if (!(blockEntity instanceof MenuProvider menuProvider)) {
            return false;
        }

        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();

        if (block instanceof ChestBlock chestBlock) {

            // boolean flag: do not check if chest is blocked
            menuProvider = chestBlock.getMenuProvider(blockState, level, blockPos, true);

            if (menuProvider == null) {
                OpenInv.getPlugin(OpenInv.class).sendSystemMessage(bukkitPlayer, "messages.error.lootNotGenerated");
                return false;
            }

            if (block instanceof TrappedChestBlock) {
                bukkitPlayer.incrementStatistic(Statistic.TRAPPED_CHEST_TRIGGERED);
            } else {
                bukkitPlayer.incrementStatistic(Statistic.CHEST_OPENED);
            }
        }

        if (block instanceof ShulkerBoxBlock) {
            bukkitPlayer.incrementStatistic(Statistic.SHULKER_BOX_OPENED);
        }

        if (block instanceof BarrelBlock) {
            bukkitPlayer.incrementStatistic(Statistic.OPEN_BARREL);
        }

        // AnyChest only - SilentChest not active, container unsupported, or unnecessary.
        if (!silentchest || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            player.openMenu(menuProvider);
            return true;
        }

        // SilentChest requires access to setting players' game mode directly.
        if (this.serverPlayerGameModeGameType == null) {
            return false;
        }

        if (blockEntity instanceof RandomizableContainerBlockEntity lootable) {
            if (lootable.lootTable != null) {
                OpenInv.getPlugin(OpenInv.class).sendSystemMessage(bukkitPlayer, "messages.error.lootNotGenerated");
                return false;
            }
        }

        GameType gameType = player.gameMode.getGameModeForPlayer();
        this.forceGameType(player, GameType.SPECTATOR);
        player.openMenu(menuProvider);
        this.forceGameType(player, gameType);
        return true;
    }

    @Override
    public void deactivateContainer(@NotNull final Player bukkitPlayer) {
        if (this.serverPlayerGameModeGameType == null || bukkitPlayer.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        ServerPlayer player = PlayerDataManager.getHandle(bukkitPlayer);

        // Force game mode change without informing plugins or players.
        // Regular game mode set calls GameModeChangeEvent and is cancellable.
        GameType gameType = player.gameMode.getGameModeForPlayer();
        this.forceGameType(player, GameType.SPECTATOR);

        // ServerPlayer#closeContainer cannot be called without entering an
        // infinite loop because this method is called during inventory close.
        // From ServerPlayer#closeContainer -> CraftEventFactory#handleInventoryCloseEvent
        player.containerMenu.transferTo(player.inventoryMenu, player.getBukkitEntity());
        // From ServerPlayer#closeContainer
        player.doCloseContainer();
        // Regular inventory close will handle the rest - packet sending, etc.

        // Revert forced game mode.
        this.forceGameType(player, gameType);
    }

    private void forceGameType(final ServerPlayer player, final GameType gameMode) {
        if (this.serverPlayerGameModeGameType == null) {
            // No need to warn repeatedly, error on startup and lack of function should be enough.
            return;
        }
        try {
            this.serverPlayerGameModeGameType.setAccessible(true);
            this.serverPlayerGameModeGameType.set(player.gameMode, gameMode);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
