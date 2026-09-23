package com.lishid.openinv.internal.paper1_21_11.container;

import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import com.lishid.openinv.internal.paper1_21_11.container.menu.OpenInventoryMenu;
import com.lishid.openinv.internal.paper26_3.container.menu.OpenChestMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenInventory extends com.lishid.openinv.internal.paper26_3.container.OpenInventory {

  public OpenInventory(
      InventoryFactory<ServerPlayer, ItemStack, Container, Slot, EquipmentSlot> factory,
      org.bukkit.entity.Player bukkitPlayer
  ) {
    super(factory, bukkitPlayer);
  }

  @Override
  public @Nullable OpenChestMenu<?> createMenu(Player player, int i, boolean viewOnly) {
    if (player instanceof ServerPlayer serverPlayer) {
      return new OpenInventoryMenu(factory, this, serverPlayer, i, viewOnly);
    }
    return null;
  }

}
