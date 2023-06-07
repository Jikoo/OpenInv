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

import com.mojang.logging.LogUtils;
import java.io.File;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;

public class OpenPlayer extends CraftPlayer {

    public OpenPlayer(CraftServer server, ServerPlayer entity) {
        super(server, entity);
    }

    @Override
    public void loadData() {
        // See CraftPlayer#loadData
        CompoundTag loaded = this.server.getHandle().playerIo.load(this.getHandle());
        if (loaded != null) {
            getHandle().readAdditionalSaveData(loaded);
        }
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
                // Special case: save old vehicle data
                CompoundTag oldData = worldNBTStorage.load(player);

                if (oldData != null && oldData.contains("RootVehicle", 10)) {
                    // See net.minecraft.server.PlayerList#a(NetworkManager, EntityPlayer) and net.minecraft.server.EntityPlayer#b(NBTTagCompound)
                    playerData.put("RootVehicle", oldData.getCompound("RootVehicle"));
                }
            }

            File file = File.createTempFile(player.getStringUUID() + "-", ".dat", worldNBTStorage.getPlayerDir());
            NbtIo.writeCompressed(playerData, file);
            File file1 = new File(worldNBTStorage.getPlayerDir(), player.getStringUUID() + ".dat");
            File file2 = new File(worldNBTStorage.getPlayerDir(), player.getStringUUID() + ".dat_old");
            Util.safeReplaceFile(file1, file, file2);
        } catch (Exception e) {
            LogUtils.getLogger().warn("Failed to save player data for {}: {}", player.getScoreboardName(), e);
        }
    }

}
