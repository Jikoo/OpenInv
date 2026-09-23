package com.lishid.openinv.internal.paper26_1.player;

import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public class PlayerManager extends com.lishid.openinv.internal.paper26_3.player.PlayerManager {

  public PlayerManager(
      Logger logger,
      InventoryFactory<ServerPlayer, ItemStack, Container, Slot, EquipmentSlot> factory
  ) {
    super(logger, factory);
  }

  @Override
  protected void removeAdvancementListeners(ServerPlayer entity) {
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
