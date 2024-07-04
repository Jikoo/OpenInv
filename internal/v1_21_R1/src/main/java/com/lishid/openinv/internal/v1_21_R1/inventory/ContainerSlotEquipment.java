package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * A slot for equipment that displays placeholders if empty.
 */
class ContainerSlotEquipment extends ContainerSlotList {

  private final ItemStack placeholder;
  private final EquipmentSlot equipmentSlot;

  ContainerSlotEquipment(ServerPlayer holder, int index, EquipmentSlot equipmentSlot) {
    super(holder, index, InventoryType.SlotType.ARMOR);
    placeholder = switch (equipmentSlot) {
      case HEAD -> PlaceholderManager.emptyHelmet;
      case CHEST -> PlaceholderManager.emptyChestplate;
      case LEGS -> PlaceholderManager.emptyLeggings;
      case FEET -> PlaceholderManager.emptyBoots;
      default -> PlaceholderManager.emptyOffHand;
    };
    this.equipmentSlot = equipmentSlot;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.items = holder.getInventory().armor;
  }

  @Override
  public Slot asMenuSlot(Container container, int index, int x, int y) {
    return new SlotEquipment(container, index, x, y);
  }

  class SlotEquipment extends MenuSlotPlaceholder {

    SlotEquipment(Container container, int index, int x, int y) {
      super(container, index, x, y);
    }

    @Override
    ItemStack getOrDefault() {
      ItemStack itemStack = getItem();
      if (!itemStack.isEmpty()) {
        return itemStack;
      }
      return placeholder;
    }

    EquipmentSlot getEquipmentSlot() {
      return equipmentSlot;
    }

  }

}
