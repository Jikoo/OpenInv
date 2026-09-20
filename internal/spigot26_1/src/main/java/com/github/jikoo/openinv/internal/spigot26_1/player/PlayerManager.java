package com.github.jikoo.openinv.internal.spigot26_1.player;

import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public class PlayerManager extends com.github.jikoo.openinv.internal.spigot26_3.player.PlayerManager {

  public PlayerManager(
      Logger logger,
      InventoryFactory<ServerPlayer, ItemStack, Container, Slot, EquipmentSlot> factory
  ) {
    super(logger, factory);
  }

  @Override
  protected void removeListeners(PlayerAdvancements advancements) {
    advancements.stopListening();
  }

}
