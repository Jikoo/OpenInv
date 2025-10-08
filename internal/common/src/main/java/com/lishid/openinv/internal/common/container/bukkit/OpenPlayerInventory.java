package com.lishid.openinv.internal.common.container.bukkit;

import com.google.common.base.Preconditions;
import com.lishid.openinv.internal.common.container.BaseOpenInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OpenPlayerInventory extends CraftInventory implements PlayerInventory {

  public OpenPlayerInventory(@NotNull BaseOpenInventory inventory) {
    super(inventory);
  }

  @Override
  public @NotNull BaseOpenInventory getInventory() {
    return (BaseOpenInventory) super.getInventory();
  }

  @Override
  public ItemStack @NotNull [] getContents() {
    return asCraftMirror(getInventory().getOwnerHandle().getInventory().getContents());
  }

  @Override
  public void setContents(ItemStack[] items) {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    int size = internal.getContainerSize();
    Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);

    for (int index = 0; index < size; ++index) {
      if (index < items.length) {
        internal.setItem(index, CraftItemStack.asNMSCopy(items[index]));
      } else {
        internal.setItem(index, net.minecraft.world.item.ItemStack.EMPTY);
      }
    }
  }

  @Override
  public ItemStack @NotNull [] getStorageContents() {
    return asCraftMirror(getInventory().getOwnerHandle().getInventory().getNonEquipmentItems());
  }

  @Override
  public void setStorageContents(ItemStack[] items) throws IllegalArgumentException {
    NonNullList<net.minecraft.world.item.ItemStack> list = getInventory().getOwnerHandle().getInventory().getNonEquipmentItems();
    int size = list.size();
    Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);
    for (int index = 0; index < items.length; ++index) {
      list.set(index, CraftItemStack.asNMSCopy(items[index]));
    }
  }

  @Override
  public @NotNull InventoryType getType() {
    return InventoryType.PLAYER;
  }

  @Override
  public @NotNull Player getHolder() {
    return getInventory().getOwner();
  }

  @Override
  public @NotNull ItemStack @NotNull [] getArmorContents() {
    return asCraftMirror(getInventory().getOwnerHandle().getInventory().getArmorContents());
  }

  @Override
  public void setArmorContents(ItemStack @NotNull [] items) {
    int size = Inventory.EQUIPMENT_SLOTS_SORTED_BY_INDEX.length;
    Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);
    for (int index = 0; index < items.length; ++index) {
      getInventory().getOwnerHandle().getInventory().equipment.set(
          Inventory.EQUIPMENT_SLOTS_SORTED_BY_INDEX[index],
          CraftItemStack.asNMSCopy(items[index])
      );
    }
  }

  @Override
  public @NotNull ItemStack @NotNull [] getExtraContents() {
    return asCraftMirror(List.of(getInventory().getOwnerHandle().getInventory().equipment.get(EquipmentSlot.OFFHAND)));
  }

  @Override
  public void setExtraContents(ItemStack @NotNull [] items) {
    Preconditions.checkArgument(items.length <= 1, "items.length must be <= 1");
    for (ItemStack item : items) {
      getInventory().getOwnerHandle().getInventory().equipment.set(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(item));
    }
  }

  @Override
  public @NotNull ItemStack getHelmet() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().equipment
        .get(EquipmentSlot.HEAD));
  }

  @Override
  public void setHelmet(@Nullable ItemStack helmet) {
    getInventory().getOwnerHandle().getInventory().equipment
        .set(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(helmet));
  }

  @Override
  public @NotNull ItemStack getChestplate() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().equipment
        .get(EquipmentSlot.CHEST));
  }

  @Override
  public void setChestplate(@Nullable ItemStack chestplate) {
    getInventory().getOwnerHandle().getInventory().equipment
        .set(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(chestplate));
  }

  @Override
  public @NotNull ItemStack getLeggings() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().equipment
        .get(EquipmentSlot.LEGS));
  }

  @Override
  public void setLeggings(@Nullable ItemStack leggings) {
    getInventory().getOwnerHandle().getInventory().equipment
        .set(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(leggings));
  }

  @Override
  public @NotNull ItemStack getBoots() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().equipment
        .get(EquipmentSlot.FEET));
  }

  @Override
  public void setBoots(@Nullable ItemStack boots) {
    getInventory().getOwnerHandle().getInventory().equipment
        .set(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(boots));
  }

  @Override
  public @NotNull ItemStack getItemInMainHand() {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    return CraftItemStack.asCraftMirror(internal.getSelectedItem());
  }

  @Override
  public void setItemInMainHand(@Nullable ItemStack item) {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    internal.setSelectedItem(CraftItemStack.asNMSCopy(item));
  }

  @Override
  public @NotNull ItemStack getItemInOffHand() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().equipment
        .get(EquipmentSlot.OFFHAND));
  }

  @Override
  public void setItemInOffHand(@Nullable ItemStack item) {
    getInventory().getOwnerHandle().getInventory().equipment
        .set(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(item));
  }

  @SuppressWarnings("InlineMeSuggester")
  @Deprecated
  @Override
  public @NotNull ItemStack getItemInHand() {
    return getItemInMainHand();
  }

  @SuppressWarnings("InlineMeSuggester")
  @Deprecated
  @Override
  public void setItemInHand(@Nullable ItemStack stack) {
    setItemInMainHand(stack);
  }

  @Override
  public int getHeldItemSlot() {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    return internal.getNonEquipmentItems().size() - 9 + internal.getSelectedSlot();
  }

  @Override
  public void setHeldItemSlot(int slot) {
    slot %= 9;
    getInventory().getOwnerHandle().getInventory().setSelectedSlot(slot);
  }

  @Override
  public @NotNull ItemStack getItem(@NotNull org.bukkit.inventory.EquipmentSlot slot) {
    return switch (slot) {
      case HAND -> getItemInMainHand();
      case OFF_HAND -> getItemInOffHand();
      case FEET -> getBoots();
      case LEGS -> getLeggings();
      case CHEST -> getChestplate();
      case HEAD -> getHelmet();
      default -> throw new IllegalArgumentException("Unsupported EquipmentSlot " + slot);
    };
  }

  @Override
  public void setItem(@NotNull org.bukkit.inventory.EquipmentSlot slot, @Nullable ItemStack item) {
    switch (slot) {
      case HAND -> setItemInMainHand(item);
      case OFF_HAND -> setItemInOffHand(item);
      case FEET -> setBoots(item);
      case LEGS -> setLeggings(item);
      case CHEST -> setChestplate(item);
      case HEAD -> setHelmet(item);
      default -> throw new IllegalArgumentException("Unsupported EquipmentSlot " + slot);
    }
  }

}
