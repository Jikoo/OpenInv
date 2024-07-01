package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.world.entity.player.Inventory;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenPlayerInventory extends CraftInventory implements PlayerInventory {

  public OpenPlayerInventory(@NotNull OpenInventory inventory) {
    super(inventory);
  }

  @Override
  public OpenInventory getInventory() {
    return (OpenInventory) super.getInventory();
  }

  @Override
  public @NotNull Player getHolder() {
    return getInventory().getOwner();
  }

  @Override
  public @NotNull ItemStack[] getArmorContents() {
    return asCraftMirror(getInventory().getOwnerHandle().getInventory().armor);
  }

  @Override
  public void setArmorContents(@Nullable ItemStack[] items) {
    for (int index = 0; index < items.length; ++index) {
      getInventory().getOwnerHandle().getInventory().armor.set(index, CraftItemStack.asNMSCopy(items[index]));
    }
  }

  @Override
  public @NotNull ItemStack[] getExtraContents() {
    return asCraftMirror(getInventory().getOwnerHandle().getInventory().offhand);
  }

  @Override
  public void setExtraContents(@Nullable ItemStack[] items) {
    for (int index = 0; index < items.length; ++index) {
      getInventory().getOwnerHandle().getInventory().offhand.set(index, CraftItemStack.asNMSCopy(items[index]));
    }
  }

  @Override
  public @Nullable ItemStack getHelmet() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().getArmor(3));
  }

  @Override
  public void setHelmet(@Nullable ItemStack helmet) {
    getInventory().getOwnerHandle().getInventory().armor.set(3, CraftItemStack.asNMSCopy(helmet));
  }

  @Override
  public @Nullable ItemStack getChestplate() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().getArmor(2));
  }

  @Override
  public void setChestplate(@Nullable ItemStack chestplate) {
    getInventory().getOwnerHandle().getInventory().armor.set(2, CraftItemStack.asNMSCopy(chestplate));
  }

  @Override
  public @Nullable ItemStack getLeggings() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().getArmor(1));
  }

  @Override
  public void setLeggings(@Nullable ItemStack leggings) {
    getInventory().getOwnerHandle().getInventory().armor.set(1, CraftItemStack.asNMSCopy(leggings));
  }

  @Override
  public @Nullable ItemStack getBoots() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().getArmor(0));
  }

  @Override
  public void setBoots(@Nullable ItemStack boots) {
    getInventory().getOwnerHandle().getInventory().armor.set(0, CraftItemStack.asNMSCopy(boots));
  }

  @Override
  public @NotNull ItemStack getItemInMainHand() {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    return CraftItemStack.asCraftMirror(internal.getItem(internal.selected));
  }

  @Override
  public void setItemInMainHand(@Nullable ItemStack item) {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    internal.setItem(internal.selected, CraftItemStack.asNMSCopy(item));
  }

  @Override
  public @NotNull ItemStack getItemInOffHand() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().offhand.get(0));
  }

  @Override
  public void setItemInOffHand(@Nullable ItemStack item) {
    getInventory().getOwnerHandle().getInventory().offhand.set(0, CraftItemStack.asNMSCopy(item));
  }

  @Override
  public @NotNull ItemStack getItemInHand() {
    return getItemInMainHand();
  }

  @Override
  public void setItemInHand(@Nullable ItemStack stack) {
    setItemInMainHand(stack);
  }

  @Override
  public int getHeldItemSlot() {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    return internal.items.size() - 9 + internal.selected;
  }

  @Override
  public void setHeldItemSlot(int slot) {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    if (slot < internal.items.size()) {
      slot += internal.items.size() - 9;
    }
    internal.selected = slot;
  }

  @Override
  public @Nullable ItemStack getItem(@NotNull EquipmentSlot slot) {
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
  public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item) {
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
