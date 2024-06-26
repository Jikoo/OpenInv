package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

interface ContainerSlot {

  void setHolder(@NotNull ServerPlayer holder);

  ItemStack get();

  ItemStack remove();

  ItemStack removePartial(int amount);

  void set(ItemStack itemStack);

  Slot asMenuSlot(Container container, int index, int x, int y);

  org.bukkit.event.inventory.InventoryType.SlotType getSlotType();

}
