package com.lishid.openinv.internal.paper1_21_10.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class PlayerManager extends com.lishid.openinv.internal.common.player.PlayerManager {

  public PlayerManager(@NotNull Logger logger) {
    super(logger);
  }

  @Override
  protected void injectPlayer(@NotNull MinecraftServer server, @NotNull ServerPlayer player) throws IllegalAccessException {
    if (bukkitEntity == null) {
      return;
    }

    bukkitEntity.setAccessible(true);

    bukkitEntity.set(player, new OpenPlayer(server.server, player, this));
  }

}
