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

class CursorContainerSlot implements ContainerSlot {

  private @NotNull ServerPlayer holder;

  CursorContainerSlot(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public ItemStack get() {
    if (holder.connection == null || holder.connection.isDisconnected()) {
      ItemStack itemStack = new ItemStack(Items.STRUCTURE_VOID);
      // "Not available - Offline"
      itemStack.set(DataComponents.CUSTOM_NAME,
          Component.translatable("options.narrator.notavailable")
              .withStyle(style -> style.withItalic(false))
              .append(Component.literal(" - "))
              .append(Component.translatable("gui.socialInteractions.status_offline")));
      return itemStack;
    }
    if (holder.gameMode.isCreative()) {
      ItemStack itemStack = new ItemStack(Items.STRUCTURE_VOID);
      // "Blocked - Creative Mode"
      itemStack.set(
          DataComponents.CUSTOM_NAME,
          Component.translatable("options.narrator.notavailable")
              .withStyle(style -> style.withItalic(false))
              .append(" - ")
              .append(Component.translatable("gameMode.creative")));
      return itemStack;
    }
    return holder.containerMenu.getCarried();
  }

  @Override
  public ItemStack remove() {
    ItemStack carried = holder.containerMenu.getCarried();
    holder.containerMenu.setCarried(ItemStack.EMPTY);
    return carried;
  }

  @Override
  public ItemStack removePartial(int amount) {
    ItemStack carried = holder.containerMenu.getCarried();
    if (!carried.isEmpty() && carried.getCount() >= amount) {
      ItemStack value = carried.split(amount);
      if (carried.isEmpty()) {
        holder.containerMenu.setCarried(ItemStack.EMPTY);
      }
      return value;
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void set(ItemStack itemStack) {
    holder.containerMenu.setCarried(itemStack);
  }

  @Override
  public Slot asMenuSlot(Container container, int index, int x, int y) {
    return new Slot(container, index, x, y) {

      @Override
      public boolean allowModification(Player player) {
        return mayPickup(player);
      }

      @Override
      public boolean mayPickup(Player player) {
        // Owner clicking on own cursor slot deletes item.
        return player != holder && slotEditable();
      }

      @Override
      public boolean mayPlace(ItemStack itemStack) {
        return slotEditable();
      }

      @Override
      public boolean hasItem() {
        return slotEditable() && super.hasItem();
      }

      private boolean slotEditable() {
        // Player must be online and not in creative - since the creative client is (semi-)authoritative,
        // it ignores cursor changes without extra help, and will delete the item as a result.
        // It does not tell the server what item it has on its cursor.
        return holder.connection != null && !holder.connection.isDisconnected() && !holder.gameMode.isCreative();
      }
    };
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    // As close as possible to "not real"
    return InventoryType.SlotType.OUTSIDE;
  }

}
