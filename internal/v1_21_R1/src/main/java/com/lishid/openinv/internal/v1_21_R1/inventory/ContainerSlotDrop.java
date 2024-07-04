package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * A fake slot used to drop items. Unavailable offline.
 */
class ContainerSlotDrop implements ContainerSlot {

  private ServerPlayer holder;

  ContainerSlotDrop(@NotNull ServerPlayer holder) {
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
    holder.drop(itemStack, true);
  }

  @Override
  public Slot asMenuSlot(Container container, int index, int x, int y) {
    return new SlotDrop(container, index, x, y);
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    // Behaves like dropping an item outside the screen, just by the target player.
    return InventoryType.SlotType.OUTSIDE;
  }

  class SlotDrop extends MenuSlotPlaceholder {

    private SlotDrop(Container container, int index, int x, int y) {
      super(container, index, x, y);
    }

    @Override
    ItemStack getOrDefault() {
      return holder.connection != null && !holder.connection.isDisconnected()
          ? PlaceholderManager.drop
          : PlaceholderManager.blockedOffline;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
      return holder.connection != null && !holder.connection.isDisconnected();
    }

    @Override
    public boolean hasItem() {
      return false;
    }

    @Override
    public boolean isFake() {
      return true;
    }

  }

}
