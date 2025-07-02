package com.lishid.openinv.internal.reobf.container.slot;

import com.lishid.openinv.internal.reobf.container.slot.placeholder.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R5.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_21_R5.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class ContentEquipment implements Content {

  private PlayerInventory equipment;
  private final ItemStack placeholder;
  private final org.bukkit.inventory.EquipmentSlot equipmentSlot;

  public ContentEquipment(ServerPlayer holder, EquipmentSlot equipmentSlot) {
    setHolder(holder);
    placeholder = switch (equipmentSlot) {
      case HEAD -> Placeholders.emptyHelmet;
      case CHEST -> Placeholders.emptyChestplate;
      case LEGS -> Placeholders.emptyLeggings;
      case FEET -> Placeholders.emptyBoots;
      default -> Placeholders.emptyOffHand;
    };
    this.equipmentSlot = CraftEquipmentSlot.getSlot(equipmentSlot);
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.equipment = holder.getBukkitEntity().getInventory();
  }

  @Override
  public ItemStack get() {
    return CraftItemStack.asNMSCopy(equipment.getItem(equipmentSlot));
  }

  @Override
  public ItemStack remove() {
    org.bukkit.inventory.ItemStack old = equipment.getItem(equipmentSlot);
    equipment.setItem(equipmentSlot, null);
    return CraftItemStack.asNMSCopy(old);
  }

  @Override
  public ItemStack removePartial(int amount) {
    if (amount <= 0) {
      return ItemStack.EMPTY;
    }
    ItemStack current = get();
    if (current.isEmpty()) {
      return ItemStack.EMPTY;
    }
    ItemStack split = current.split(amount);
    set(current);
    return split;
  }

  @Override
  public void set(ItemStack itemStack) {
    equipment.setItem(equipmentSlot, CraftItemStack.asCraftMirror(itemStack));
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
      return CraftEquipmentSlot.getNMS(equipmentSlot);
    }

    public void onlyEquipmentFor(ServerPlayer viewer) {
      this.viewer = viewer;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack itemStack) {
      if (viewer == null) {
        return true;
      }

      return equipmentSlot == org.bukkit.inventory.EquipmentSlot.OFF_HAND
          || viewer.getEquipmentSlotForItem(itemStack) == CraftEquipmentSlot.getNMS(equipmentSlot);
    }

  }

}
