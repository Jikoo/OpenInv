package com.lishid.openinv.internal.common.player;

import com.lishid.openinv.event.OpenEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.craftbukkit.CraftServer;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

public class OpenPlayer extends BaseOpenPlayer {

  protected OpenPlayer(
      CraftServer server,
      ServerPlayer entity,
      PlayerManager manager
  ) {
    super(server, entity, manager);
  }

  @Override
  public void saveData() {
    if (OpenEvents.saveCancelled(this)) {
      return;
    }

    ServerPlayer player = this.getHandle();
    Logger logger = LogUtils.getLogger();
    // See net.minecraft.world.level.storage.PlayerDataStorage#save(EntityHuman)
    try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), logger)) {
      PlayerDataStorage worldNBTStorage = server.getServer().getPlayerList().playerIo;

      CompoundTag oldData = isOnline()
          ? null
          : worldNBTStorage.load(player.getName().getString(), player.getStringUUID(), scopedCollector).orElse(null);
      CompoundTag playerData = getWritableTag(oldData);

      ValueOutput valueOutput = TagValueOutput.createWrappingWithContext(scopedCollector, player.registryAccess(), playerData);
      player.saveWithoutId(valueOutput);

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
