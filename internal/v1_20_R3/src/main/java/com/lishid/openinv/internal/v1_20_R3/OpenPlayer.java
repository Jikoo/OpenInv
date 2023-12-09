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

package com.lishid.openinv.internal.v1_20_R3;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public class OpenPlayer extends CraftPlayer {

    public OpenPlayer(CraftServer server, ServerPlayer entity) {
        super(server, entity);
    }

    @Override
    public void loadData() {
        PlayerDataManager.loadData(getHandle());
    }

    @Override
    public void saveData() {
        ServerPlayer player = this.getHandle();
        // See net.minecraft.world.level.storage.PlayerDataStorage#save(EntityHuman)
        try {
            PlayerDataStorage worldNBTStorage = player.server.getPlayerList().playerIo;

            CompoundTag playerData = player.saveWithoutId(new CompoundTag());
            setExtraData(playerData);

            if (!isOnline()) {
                // Preserve certain data when offline.
                CompoundTag oldData = worldNBTStorage.load(player);
                revertSpecialValues(playerData, oldData);
            }

            Path playerDataDir = worldNBTStorage.getPlayerDir().toPath();
            Path file = Files.createTempFile(playerDataDir, player.getStringUUID() + "-", ".dat");
            NbtIo.writeCompressed(playerData, file);
            Path dataFile = playerDataDir.resolve(player.getStringUUID() + ".dat");
            Path backupFile = playerDataDir.resolve(player.getStringUUID() + ".dat_old");
            Util.safeReplaceFile(dataFile, file, backupFile);
        } catch (Exception e) {
            LogUtils.getLogger().warn("Failed to save player data for {}: {}", player.getScoreboardName(), e);
        }
    }

    private void revertSpecialValues(@NotNull CompoundTag newData, @Nullable CompoundTag oldData) {
        if (oldData == null) {
            return;
        }

        // Prevent vehicle deletion.
        if (oldData.contains("RootVehicle", Tag.TAG_COMPOUND)) {
            // See net.minecraft.server.players.PlayerList#save(ServerPlayer)
            // See net.minecraft.server.level.ServerPlayer#addAdditionalSaveData(CompoundTag)
            try {
                Tag attach = oldData.get("Attach");
                if (attach != null) {
                    newData.putUUID("Attach", NbtUtils.loadUUID(attach));
                }
            } catch (IllegalArgumentException ignored) {
                // Likely will not re-mount successfully, but at least the mount will not be deleted.
            }
            newData.put("Entity", oldData.getCompound("Entity"));
            newData.put("RootVehicle", oldData.getCompound("RootVehicle"));
        }

        // Revert automatic updates to play timestamps.
        copyValue(oldData, newData, "bukkit", "lastPlayed", NumericTag.class);
        copyValue(oldData, newData, "Paper", "LastSeen", NumericTag.class);
        copyValue(oldData, newData, "Paper", "LastLogin", NumericTag.class);
    }

    private <T extends Tag> void copyValue(
        @NotNull CompoundTag source,
        @NotNull CompoundTag target,
        @NotNull String container,
        @NotNull String key,
        @NotNull Class<T> tagType) {
        CompoundTag oldContainer = getTag(source, container, CompoundTag.class);
        CompoundTag newContainer = getTag(target, container, CompoundTag.class);

        // Container being null means the server implementation doesn't store this data.
        if (oldContainer == null || newContainer == null) {
            return;
        }

        // If old tag exists, copy it to new location, removing otherwise.
        setTag(newContainer, key, getTag(oldContainer, key, tagType));
    }

    private <T extends Tag> @Nullable T getTag(
        @NotNull CompoundTag container,
        @NotNull String key,
        @NotNull Class<T> dataType) {
        Tag value = container.get(key);
        if (value == null || !dataType.isAssignableFrom(value.getClass())) {
            return null;
        }
        return dataType.cast(value);
    }

    private <T extends Tag> void setTag(
        @NotNull CompoundTag container,
        @NotNull String key,
        @Nullable T data) {
        if (data == null) {
            container.remove(key);
        } else {
            container.put(key, data);
        }
    }

}
