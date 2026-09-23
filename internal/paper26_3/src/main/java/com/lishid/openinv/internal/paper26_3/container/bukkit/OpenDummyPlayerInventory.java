package com.lishid.openinv.internal.paper26_3.container.bukkit;

import net.minecraft.world.Container;
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
  public ItemStack getHelmet() {
    return ItemStack.empty();
  }

  @Override
  public ItemStack getChestplate() {
    return ItemStack.empty();
  }

  @Override
  public ItemStack getLeggings() {
    return ItemStack.empty();
  }

  @Override
  public ItemStack getBoots() {
    return ItemStack.empty();
  }

  @Override
  public void setItem(EquipmentSlot slot, @Nullable ItemStack item) {

  }

  @Override
  public ItemStack getItem(EquipmentSlot slot) {
    return ItemStack.empty();
  }

  @Override
  public void setArmorContents(@Nullable ItemStack[] items) {

  }

  @Override
  public void setExtraContents(@Nullable ItemStack[] items) {

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
    return ItemStack.empty();
  }

  @Override
  public void setItemInMainHand(@Nullable ItemStack item) {

  }

  @Override
  public ItemStack getItemInOffHand() {
    return ItemStack.empty();
  }

  @Override
  public void setItemInOffHand(@Nullable ItemStack item) {

  }

  @SuppressWarnings("InlineMeSuggester")
  @Deprecated
  @Override
  public ItemStack getItemInHand() {
    return ItemStack.empty();
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
