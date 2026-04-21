package com.lishid.openinv.internal.paper1_21_11.container.slot;

import com.lishid.openinv.internal.paper1_21_11.container.slot.placeholder.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * A slot for equipment that displays placeholders if empty.
 */
public class ContentEquipment implements Content {

  private EntityEquipment equipment;
  private final ItemStack placeholder;
  private final EquipmentSlot equipmentSlot;

  public ContentEquipment(ServerPlayer holder, EquipmentSlot equipmentSlot) {
    setHolder(holder);
    placeholder = switch (equipmentSlot) {
      case HEAD -> Placeholders.emptyHelmet;
      case CHEST -> Placeholders.emptyChestplate;
      case LEGS -> Placeholders.emptyLeggings;
      case FEET -> Placeholders.emptyBoots;
      default -> Placeholders.emptyOffHand;
    };
    this.equipmentSlot = equipmentSlot;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.equipment = holder.getInventory().equipment;
  }

  @Override
  public ItemStack get() {
    return equipment.get(equipmentSlot);
  }

  @Override
  public ItemStack remove() {
    return equipment.set(equipmentSlot, ItemStack.EMPTY);
  }

  @Override
  public ItemStack removePartial(int amount) {
    ItemStack current = get();
    if (!current.isEmpty() && amount > 0) {
      return current.split(amount);
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void set(ItemStack itemStack) {
    equipment.set(equipmentSlot, itemStack);
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotEquipment(container, slot, x, y);
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return InventoryType.SlotType.ARMOR;
  }

  public class SlotEquipment extends SlotPlaceholder {

    private ServerPlayer viewer;

    SlotEquipment(Container container, int index, int x, int y) {
      super(container, index, x, y);
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
    public boolean mayPlace(@NotNull ItemStack itemStack) {
      if (viewer == null) {
        return true;
      }

      return equipmentSlot == EquipmentSlot.OFFHAND || viewer.getEquipmentSlotForItem(itemStack) == equipmentSlot;
    }

  }

}
