package com.lishid.openinv.internal.paper1_21_5.player;

import com.lishid.openinv.internal.common.player.BaseOpenPlayer;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class OpenPlayer extends BaseOpenPlayer {

  protected OpenPlayer(CraftServer server, ServerPlayer entity, PlayerManager manager) {
    super(server, entity, manager);
  }

  @Override
  protected void trySave(ServerPlayer player) {
    // See net.minecraft.world.level.storage.PlayerDataStorage#save(EntityHuman)
    try {
      PlayerDataStorage worldNBTStorage = player.server.getPlayerList().playerIo;

      CompoundTag oldData = isOnline() ? null : worldNBTStorage.load(player.getName().getString(), player.getStringUUID()).orElse(null);
      CompoundTag playerData = getWritableTag(oldData);

      playerData = player.saveWithoutId(playerData);

      saveSafe(player, oldData, playerData, worldNBTStorage);
    } catch (Exception e) {
      LogUtils.getLogger().warn("Failed to save player data for {}: {}", player.getScoreboardName(), e);
    }
  }

  @Override
  protected void safeReplaceFile(@NotNull Path dataFile, @NotNull Path tempFile, @NotNull Path backupFile) {
    net.minecraft.Util.safeReplaceFile(dataFile, tempFile, backupFile);
  }

  @Override
  protected void remove(@NotNull CompoundTag tag, @NotNull String key) {
    tag.remove(key);
  }

}
