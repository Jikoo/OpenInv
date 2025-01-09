package com.lishid.openinv.internal.v1_21_R2.container.bukkit;

import net.minecraft.world.Container;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenDummyPlayerInventory extends OpenDummyInventory implements PlayerInventory {

  public OpenDummyPlayerInventory(Container inventory) {
    super(inventory, InventoryType.PLAYER);
  }

  @Override
  public HumanEntity getHolder() {
    return (HumanEntity) super.getHolder();
  }

  @Override
  public @NotNull ItemStack[] getArmorContents() {
    return new ItemStack[4];
  }

  @Override
  public @NotNull ItemStack[] getExtraContents() {
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
  public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item) {

  }

  @Override
  public @Nullable ItemStack getItem(@NotNull EquipmentSlot slot) {
    return null;
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
  public @NotNull ItemStack getItemInMainHand() {
    return new ItemStack(Material.AIR);
  }

  @Override
  public void setItemInMainHand(@Nullable ItemStack item) {

  }

  @Override
  public @NotNull ItemStack getItemInOffHand() {
    return new ItemStack(Material.AIR);
  }

  @Override
  public void setItemInOffHand(@Nullable ItemStack item) {

  }

  @Override
  public @NotNull ItemStack getItemInHand() {
    return new ItemStack(Material.AIR);
  }

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
