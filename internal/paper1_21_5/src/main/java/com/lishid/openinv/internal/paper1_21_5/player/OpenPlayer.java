package com.lishid.openinv.internal.paper1_21_5.player;

import com.lishid.openinv.event.OpenEvents;
import com.lishid.openinv.internal.common.player.BaseOpenPlayer;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.CraftServer;

import java.nio.file.Files;
import java.nio.file.Path;

public class OpenPlayer extends BaseOpenPlayer {

  protected OpenPlayer(CraftServer server, ServerPlayer entity, PlayerManager manager) {
    super(server, entity, manager);
  }

  @Override
  public void saveData() {
    if (OpenEvents.saveCancelled(this)) {
      return;
    }

    ServerPlayer player = this.getHandle();
    // See net.minecraft.world.level.storage.PlayerDataStorage#save(EntityHuman)
    try {
      PlayerDataStorage worldNBTStorage = player.server.getPlayerList().playerIo;

      CompoundTag oldData = isOnline() ? null : worldNBTStorage.load(player.getName().getString(), player.getStringUUID()).orElse(null);
      CompoundTag playerData = getWritableTag(oldData);

      playerData = player.saveWithoutId(playerData);

      if (oldData != null) {
        // Revert certain special data values when offline.
        revertSpecialValues(playerData, oldData);
      }

      Path playerDataDir = worldNBTStorage.getPlayerDir().toPath();
      Path tempFile = Files.createTempFile(playerDataDir, player.getStringUUID() + "-", ".dat");
      NbtIo.writeCompressed(playerData, tempFile);
      Path dataFile = playerDataDir.resolve(player.getStringUUID() + ".dat");
      Path backupFile = playerDataDir.resolve(player.getStringUUID() + ".dat_old");
      Util.safeReplaceFile(dataFile, tempFile, backupFile);
    } catch (Exception e) {
      LogUtils.getLogger().warn("Failed to save player data for {}: {}", player.getScoreboardName(), e);
    }
  }

}
