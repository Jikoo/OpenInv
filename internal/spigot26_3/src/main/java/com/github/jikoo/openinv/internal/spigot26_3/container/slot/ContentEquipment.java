package com.github.jikoo.openinv.internal.spigot26_3.container.slot;

import com.github.jikoo.openinv.internal.container.slot.Content;
import com.github.jikoo.openinv.internal.spigot26_3.container.slot.placeholder.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NullMarked;

/**
 * A slot for equipment that displays placeholders if empty.
 */
@NullMarked
class ContentEquipment implements Content<ServerPlayer, ItemStack, Container, Slot> {

  private PlayerInventory equipment;
  private final ItemStack placeholder;
  private final org.bukkit.inventory.EquipmentSlot equipmentSlot;

  ContentEquipment(ServerPlayer holder, EquipmentSlot equipmentSlot) {
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
  public void setHolder(ServerPlayer holder) {
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
    ItemStack current = get();
    if (!current.isEmpty() && amount > 0) {
      return current.split(amount);
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void set(ItemStack itemStack) {
    equipment.setItem(equipmentSlot, CraftItemStack.asCraftMirror(itemStack));
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotEquipment(container, slot, x, y, placeholder, CraftEquipmentSlot.getNMS(equipmentSlot));
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return InventoryType.SlotType.ARMOR;
  }

}
