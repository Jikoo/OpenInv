package com.lishid.openinv.internal.paper26_3.container.slot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NullMarked;

/**
 * A view-only slot that can't be interacted with.
 */
@NullMarked
public class ContentViewOnly implements Content {

  protected ServerPlayer holder;

  public ContentViewOnly(ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public void setHolder(ServerPlayer holder) {
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
    ContentDrop.DROP.accept(this.holder, itemStack);
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
