package com.lishid.openinv.internal.paper1_21_8.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;

public class OpenPlayer extends com.lishid.openinv.internal.paper26_1.player.OpenPlayer {

  protected OpenPlayer(
      CraftServer server,
      ServerPlayer entity,
      PlayerManager manager
  ) {
    super(server, entity, manager);
  }

  @Override
  protected Optional<CompoundTag> load(
      PlayerDataStorage storage,
      ServerPlayer player,
      ProblemReporter.ScopedCollector collector
  ) {
    return storage.load(player.getName().getString(), player.getStringUUID(), collector);
  }

  @Override
  protected void safeReplaceFile(@NotNull Path dataFile, @NotNull Path tempFile, @NotNull Path backupFile) {
    net.minecraft.Util.safeReplaceFile(dataFile, tempFile, backupFile);
  }

  @Override
  protected void remove(@NotNull CompoundTag tag, @NotNull String key) {
    // Note that the method signature here is different, though the usage
    // appears identical. This version does not return the removed tag.
    tag.remove(key);
  }

}
