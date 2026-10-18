package com.lishid.openinv.internal.paper26_3.container.bukkit;

import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import com.lishid.openinv.internal.paper26_3.container.BaseOpenInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class OpenPlayerInventorySelf extends OpenPlayerInventory {

  private final int offset;

  public OpenPlayerInventorySelf(
      InventoryFactory<ServerPlayer, net.minecraft.world.item.ItemStack, Container, Slot, EquipmentSlot> factory,
      BaseOpenInventory inventory,
      int offset
  ) {
    super(factory, inventory);
    this.offset = offset;
  }

  @Override
  public @Nullable ItemStack getItem(int index) {
    return super.getItem(offset + index);
  }

  @Override
  public void setItem(int index, @Nullable ItemStack item) {
    super.setItem(offset + index, item);
  }

}
