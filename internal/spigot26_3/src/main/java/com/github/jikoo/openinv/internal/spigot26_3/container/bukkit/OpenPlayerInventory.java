package com.github.jikoo.openinv.internal.spigot26_3.container.bukkit;

import com.github.jikoo.openinv.internal.spigot26_3.container.OpenInventory;
import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class OpenPlayerInventory extends CraftInventory implements PlayerInventory {

  public OpenPlayerInventory(OpenInventory inventory) {
    super(inventory);
  }

  @Override
  public OpenInventory getInventory() {
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
  public InventoryType getType() {
    return InventoryType.PLAYER;
  }

  @Override
  public Player getHolder() {
    return getInventory().getOwner();
  }

  @Override
  public ItemStack[] getArmorContents() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getArmorContents();
  }

  @Override
  public void setArmorContents(ItemStack[] items) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setArmorContents(items);
  }

  @Override
  public ItemStack[] getExtraContents() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getExtraContents();
  }

  @Override
  public void setExtraContents(ItemStack[] items) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setExtraContents(items);
  }

  @Override
  public @Nullable ItemStack getHelmet() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getHelmet();
  }

  @Override
  public void setHelmet(@Nullable ItemStack helmet) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setHelmet(helmet);
  }

  @Override
  public @Nullable ItemStack getChestplate() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getChestplate();
  }

  @Override
  public void setChestplate(@Nullable ItemStack chestplate) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setChestplate(chestplate);
  }

  @Override
  public @Nullable ItemStack getLeggings() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getLeggings();
  }

  @Override
  public void setLeggings(@Nullable ItemStack leggings) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setLeggings(leggings);
  }

  @Override
  public @Nullable ItemStack getBoots() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getBoots();
  }

  @Override
  public void setBoots(@Nullable ItemStack boots) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setBoots(boots);
  }

  @Override
  public ItemStack getItemInMainHand() {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    return CraftItemStack.asCraftMirror(internal.getSelectedItem());
  }

  @Override
  public void setItemInMainHand(@Nullable ItemStack item) {
    Inventory internal = getInventory().getOwnerHandle().getInventory();
    internal.setSelectedItem(CraftItemStack.asNMSCopy(item));
  }

  @Override
  public ItemStack getItemInOffHand() {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getItemInOffHand();
  }

  @Override
  public void setItemInOffHand(@Nullable ItemStack item) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setItemInOffHand(item);
  }

  @SuppressWarnings("InlineMeSuggester")
  @Deprecated
  @Override
  public ItemStack getItemInHand() {
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
  public @Nullable ItemStack getItem(org.bukkit.inventory.EquipmentSlot slot) {
    return getInventory().getOwnerHandle().getBukkitEntity().getInventory().getItem(slot);
  }

  @Override
  public void setItem(org.bukkit.inventory.EquipmentSlot slot, @Nullable ItemStack item) {
    getInventory().getOwnerHandle().getBukkitEntity().getInventory().setItem(slot, item);
  }

}
