package com.lishid.openinv.internal.paper1_21_8.player;

import com.lishid.openinv.internal.common.player.BaseOpenPlayer;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

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
  protected void trySave(ServerPlayer player) {
    Logger logger = LogUtils.getLogger();
    // See net.minecraft.world.level.storage.PlayerDataStorage#save(EntityHuman)
    try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), logger)) {
      PlayerDataStorage worldNbtStorage = server.getServer().getPlayerList().playerIo;

      CompoundTag oldData = isOnline()
          ? null
          : worldNbtStorage.load(player.getName().getString(), player.getStringUUID(), scopedCollector).orElse(null);
      CompoundTag playerData = getWritableTag(oldData);

      ValueOutput valueOutput = TagValueOutput.createWrappingWithContext(scopedCollector, player.registryAccess(), playerData);
      player.saveWithoutId(valueOutput);

      saveSafe(player, oldData, playerData, worldNbtStorage);
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
