package com.lishid.openinv.internal.paper1_21_10.player;

import com.lishid.openinv.internal.common.player.PlayerManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class OpenPlayer extends com.lishid.openinv.internal.common.player.OpenPlayer {

  protected OpenPlayer(
      CraftServer server,
      ServerPlayer entity,
      PlayerManager manager
  ) {
    super(server, entity, manager);
  }

  @Override
  protected void safeReplaceFile(
      @NotNull Path dataFile,
      @NotNull Path tempFile,
      @NotNull Path backupFile
  ) {
    net.minecraft.Util.safeReplaceFile(dataFile, tempFile, backupFile);
  }

  @Override
  protected void remove(@NotNull CompoundTag tag, @NotNull String key) {
    tag.remove(key);
  }

}
