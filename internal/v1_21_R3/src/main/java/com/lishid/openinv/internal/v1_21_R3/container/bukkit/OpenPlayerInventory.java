package com.lishid.openinv.internal.v1_21_R3.container.bukkit;

import com.google.common.base.Preconditions;
import com.lishid.openinv.internal.v1_21_R3.container.OpenInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenPlayerInventory extends CraftInventory implements PlayerInventory {

  public OpenPlayerInventory(@NotNull OpenInventory inventory) {
    super(inventory);
  }

  @Override
  public @NotNull OpenInventory getInventory() {
    return (OpenInventory) super.getInventory();
  }

  @Override
  public ItemStack[] getContents() {
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
  public ItemStack[] getStorageContents() {
    return asCraftMirror(getInventory().getOwnerHandle().getInventory().items);
  }

  @Override
  public void setStorageContents(ItemStack[] items) throws IllegalArgumentException {
    NonNullList<net.minecraft.world.item.ItemStack> list = getInventory().getOwnerHandle().getInventory().items;
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
  public @NotNull ItemStack[] getArmorContents() {
    return asCraftMirror(getInventory().getOwnerHandle().getInventory().armor);
  }

  @Override
  public void setArmorContents(@Nullable ItemStack[] items) {
    NonNullList<net.minecraft.world.item.ItemStack> list = getInventory().getOwnerHandle().getInventory().armor;
    int size = list.size();
    Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);
    for (int index = 0; index < items.length; ++index) {
      list.set(index, CraftItemStack.asNMSCopy(items[index]));
    }
  }

  @Override
  public @NotNull ItemStack[] getExtraContents() {
    return asCraftMirror(getInventory().getOwnerHandle().getInventory().offhand);
  }

  @Override
  public void setExtraContents(@Nullable ItemStack[] items) {
    NonNullList<net.minecraft.world.item.ItemStack> list = getInventory().getOwnerHandle().getInventory().offhand;
    int size = list.size();
    Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);
    for (int index = 0; index < items.length; ++index) {
      list.set(index, CraftItemStack.asNMSCopy(items[index]));
    }
  }

  @Override
  public @Nullable ItemStack getHelmet() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory()
        .getArmor(EquipmentSlot.HEAD.getIndex()));
  }

  @Override
  public void setHelmet(@Nullable ItemStack helmet) {
    getInventory().getOwnerHandle().getInventory().armor
        .set(EquipmentSlot.HEAD.getIndex(), CraftItemStack.asNMSCopy(helmet));
  }

  @Override
  public @Nullable ItemStack getChestplate() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory()
        .getArmor(EquipmentSlot.HEAD.getIndex()));
  }

  @Override
  public void setChestplate(@Nullable ItemStack chestplate) {
    getInventory().getOwnerHandle().getInventory().armor
        .set(EquipmentSlot.CHEST.getIndex(), CraftItemStack.asNMSCopy(chestplate));
  }

  @Override
  public @Nullable ItemStack getLeggings() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory()
        .getArmor(EquipmentSlot.LEGS.getIndex()));
  }

  @Override
  public void setLeggings(@Nullable ItemStack leggings) {
    getInventory().getOwnerHandle().getInventory().armor
        .set(EquipmentSlot.LEGS.getIndex(), CraftItemStack.asNMSCopy(leggings));
  }

  @Override
  public @Nullable ItemStack getBoots() {
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory()
        .getArmor(EquipmentSlot.FEET.getIndex()));
  }

  @Override
  public void setBoots(@Nullable ItemStack boots) {
    getInventory().getOwnerHandle().getInventory().armor
        .set(EquipmentSlot.FEET.getIndex(), CraftItemStack.asNMSCopy(boots));
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
    return CraftItemStack.asCraftMirror(getInventory().getOwnerHandle().getInventory().offhand.getFirst());
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
    slot %= 9;
    getInventory().getOwnerHandle().getInventory().selected = slot;
  }

  @Override
  public @Nullable ItemStack getItem(@NotNull org.bukkit.inventory.EquipmentSlot slot) {
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
