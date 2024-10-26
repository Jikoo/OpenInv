package com.lishid.openinv.internal.v1_21_R2.container.slot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * A slot for equipment that updates held items if necessary.
 */
public class ContentOffHand extends ContentEquipment {

  private ServerPlayer holder;

  public ContentOffHand(ServerPlayer holder, int localIndex) {
    super(holder, localIndex, EquipmentSlot.OFFHAND);
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.items = holder.getInventory().offhand;
    this.holder = holder;
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return InventoryType.SlotType.QUICKBAR;
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotEquipment(container, slot, x, y) {
      @Override
      public void setChanged() {
        if (holder.connection != null
            && !holder.connection.isDisconnected()
            && holder.containerMenu != holder.inventoryMenu) {
          holder.resendItemInHands();
        }
      }
    };
  }

}
