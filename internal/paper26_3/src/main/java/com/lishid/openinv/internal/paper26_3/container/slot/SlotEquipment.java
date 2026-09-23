package com.lishid.openinv.internal.paper26_3.container.slot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class SlotEquipment extends SlotPlaceholder {

  private final ItemStack placeholder;
  private final EquipmentSlot equipmentSlot;
  private @Nullable ServerPlayer viewer;

  SlotEquipment(Container container, int index, int x, int y, ItemStack placeholder, EquipmentSlot equipmentSlot) {
    super(container, index, x, y);
    this.placeholder = placeholder;
    this.equipmentSlot = equipmentSlot;
  }

  @Override
  public ItemStack getOrDefault() {
    ItemStack itemStack = getItem();
    if (!itemStack.isEmpty()) {
      return itemStack;
    }
    return placeholder;
  }

  public EquipmentSlot getEquipmentSlot() {
    return equipmentSlot;
  }

  public void onlyEquipmentFor(ServerPlayer viewer) {
    this.viewer = viewer;
  }

  @Override
  public boolean mayPlace(ItemStack itemStack) {
    if (viewer == null) {
      return true;
    }

    return equipmentSlot == EquipmentSlot.OFFHAND || viewer.getEquipmentSlotForItem(itemStack) == equipmentSlot;
  }

}
