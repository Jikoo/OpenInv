package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * A fake slot used to drop items. Unavailable offline.
 */
class ContainerSlotDrop implements ContainerSlot {

  private static final ItemStack DROP;
  private ServerPlayer holder;

  static {
    DROP = new ItemStack(Items.DROPPER);
    // Note: translatable component, not keybind component! We want the text identifying the keybind, not the key.
    DROP.set(DataComponents.CUSTOM_NAME, Component.translatable("key.drop").withStyle(style -> style.withItalic(false)));
  }

  ContainerSlotDrop(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public ItemStack get() {
    return ContainerSlot.onlineOnly(holder, () -> ItemStack.EMPTY);
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

  class SlotDrop extends Slot {

    private SlotDrop(Container container, int index, int x, int y) {
      super(container, index, x, y);
    }

    // TODO need to rework a bit for placeholder to work, breaks dropping because of swap logic.
    @Override
    public boolean mayPickup(Player var0) {
      return false;
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
