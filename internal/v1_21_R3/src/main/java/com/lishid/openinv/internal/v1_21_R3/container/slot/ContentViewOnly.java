package com.lishid.openinv.internal.v1_21_R3.container.slot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * A view-only slot that can't be interacted with.
 */
public class ContentViewOnly implements Content {

  @NotNull ServerPlayer holder;

  public ContentViewOnly(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public ItemStack get() {
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack remove() {
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack removePartial(int amount) {
    return ItemStack.EMPTY;
  }

  @Override
  public void set(ItemStack itemStack) {
    this.holder.drop(itemStack, false);
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotViewOnly(container, slot, x, y);
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return InventoryType.SlotType.OUTSIDE;
  }

}
