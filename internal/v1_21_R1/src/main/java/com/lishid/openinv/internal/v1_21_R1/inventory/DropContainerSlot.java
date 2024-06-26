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

class DropContainerSlot implements ContainerSlot {

  private ServerPlayer holder;

  DropContainerSlot(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public ItemStack get() {
    ItemStack itemStack = new ItemStack(Items.BARRIER);
    if (holder.connection == null || holder.connection.isDisconnected()) {
      itemStack.set(DataComponents.CUSTOM_NAME,
          Component.translatable("options.narrator.notavailable")
              .withStyle(style -> style.withItalic(false))
              .append(Component.literal(" - "))
              .append(Component.translatable("gui.socialInteractions.status_offline")));
    } else {
      // Note: translatable component, not keybind component! We want the text identifying the keybind, not the key.
      itemStack.set(DataComponents.CUSTOM_NAME, Component.translatable("key.drop").withStyle(style -> style.withItalic(false)));
    }
    return itemStack;
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
    return new Slot(container, index, x, y) {
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
    };
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    // Behaves like dropping an item outside the screen, just by the target player.
    return InventoryType.SlotType.OUTSIDE;
  }

}
