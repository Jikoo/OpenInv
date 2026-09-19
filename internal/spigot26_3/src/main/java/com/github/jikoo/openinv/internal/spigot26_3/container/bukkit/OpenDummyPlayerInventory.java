package com.github.jikoo.openinv.internal.spigot26_3.container.bukkit;

import net.minecraft.world.Container;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class OpenDummyPlayerInventory extends OpenDummyInventory implements PlayerInventory {

  public OpenDummyPlayerInventory(Container inventory) {
    super(inventory, InventoryType.PLAYER);
  }

  @Override
  public HumanEntity getHolder() {
    return (HumanEntity) super.getHolder();
  }

  @Override
  public ItemStack[] getArmorContents() {
    return new ItemStack[4];
  }

  @Override
  public ItemStack[] getExtraContents() {
    return new ItemStack[4];
  }

  @Override
  public @Nullable ItemStack getHelmet() {
    return null;
  }

  @Override
  public @Nullable ItemStack getChestplate() {
    return null;
  }

  @Override
  public @Nullable ItemStack getLeggings() {
    return null;
  }

  @Override
  public @Nullable ItemStack getBoots() {
    return null;
  }

  @Override
  public void setItem(EquipmentSlot slot, @Nullable ItemStack item) {

  }

  @Override
  public ItemStack getItem(EquipmentSlot slot) {
    return new ItemStack(Material.AIR);
  }

  @Override
  public void setArmorContents(ItemStack[] items) {

  }

  @Override
  public void setExtraContents(ItemStack[] items) {

  }

  @Override
  public void setHelmet(@Nullable ItemStack helmet) {

  }

  @Override
  public void setChestplate(@Nullable ItemStack chestplate) {

  }

  @Override
  public void setLeggings(@Nullable ItemStack leggings) {

  }

  @Override
  public void setBoots(@Nullable ItemStack boots) {

  }

  @Override
  public ItemStack getItemInMainHand() {
    return new ItemStack(Material.AIR);
  }

  @Override
  public void setItemInMainHand(@Nullable ItemStack item) {

  }

  @Override
  public ItemStack getItemInOffHand() {
    return new ItemStack(Material.AIR);
  }

  @Override
  public void setItemInOffHand(@Nullable ItemStack item) {

  }

  @SuppressWarnings("InlineMeSuggester")
  @Deprecated
  @Override
  public ItemStack getItemInHand() {
    return new ItemStack(Material.AIR);
  }

  @Deprecated
  @Override
  public void setItemInHand(@Nullable ItemStack stack) {

  }

  @Override
  public int getHeldItemSlot() {
    return 0;
  }

  @Override
  public void setHeldItemSlot(int slot) {

  }

}
