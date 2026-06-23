package com.lishid.openinv.internal.paper26_1.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

import java.util.logging.Logger;

public class PlayerManager extends com.lishid.openinv.internal.paper26_2.player.PlayerManager {

  public PlayerManager(Logger logger) {
    super(logger);
  }

  @Override
  protected void clearAdvancements(ServerPlayer entity) {
    entity.getAdvancements().stopListening();
  }

  @Override
  protected LevelData.RespawnData getRespawnData(ServerLevel level) {
    return level.levelData.getRespawnData();
  }

  @Override
  protected Vec3 getAdjustedSpawnLocation(
      ServerPlayer player,
      ServerLevel level,
      LevelData.RespawnData respawnData
  ) {
    return player.adjustSpawnLocation(level, respawnData.pos()).getBottomCenter();
  }

  @Override
  protected void setServerLevel(ServerPlayer player, ServerLevel level) {
    player.spawnIn(level);
  }

}
