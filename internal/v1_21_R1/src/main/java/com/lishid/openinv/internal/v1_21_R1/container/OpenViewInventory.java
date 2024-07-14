package com.lishid.openinv.internal.v1_21_R1.container;

import net.minecraft.world.Container;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * A locked down "empty" inventory that rejects plugin interaction.
 */
public class OpenViewInventory extends CraftInventory {

  public OpenViewInventory(Container inventory) {
    super(inventory);
  }

  @Override
  public @Nullable ItemStack getItem(int index) {
    return null;
  }

  @Override
  public void setItem(int index, @Nullable ItemStack item) {

  }

  @Override
  public @NotNull HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) throws IllegalArgumentException {
    return arrayToHashMap(items);
  }

  @Override
  public @NotNull HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) throws IllegalArgumentException {
    return arrayToHashMap(items);
  }

  private static @NotNull HashMap<Integer, ItemStack> arrayToHashMap(@NotNull ItemStack[] items) {
    HashMap<Integer, ItemStack> ignored = new HashMap<>();
    for (int index = 0; index < items.length; ++index) {
      ignored.put(index, items[index]);
    }
    return ignored;
  }

  @Override
  public ItemStack[] getContents() {
    return new ItemStack[getSize()];
  }

  @Override
  public void setContents(@NotNull ItemStack[] items) throws IllegalArgumentException {

  }

  @Override
  public @NotNull ItemStack[] getStorageContents() {
    return new ItemStack[getSize()];
  }

  @Override
  public void setStorageContents(@NotNull ItemStack[] items) throws IllegalArgumentException {

  }

  @Override
  public boolean contains(@NotNull Material material) throws IllegalArgumentException {
    return false;
  }

  @Override
  public boolean contains(@Nullable ItemStack item) {
    return false;
  }

  @Override
  public boolean contains(@NotNull Material material, int amount) throws IllegalArgumentException {
    return false;
  }

  @Override
  public boolean contains(@Nullable ItemStack item, int amount) {
    return false;
  }

  @Override
  public boolean containsAtLeast(@Nullable ItemStack item, int amount) {
    return false;
  }

  @Override
  public @NotNull HashMap<Integer, ItemStack> all(
      @NotNull Material material) throws IllegalArgumentException {
    return new HashMap<>();
  }

  @Override
  public @NotNull HashMap<Integer, ItemStack> all(@Nullable ItemStack item) {
    return new HashMap<>();
  }

  @Override
  public int first(@NotNull Material material) throws IllegalArgumentException {
    return -1;
  }

  @Override
  public int first(@NotNull ItemStack item) {
    return -1;
  }

  @Override
  public int firstEmpty() {
    return -1;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public void remove(@NotNull Material material) throws IllegalArgumentException {

  }

  @Override
  public void remove(@NotNull ItemStack item) {

  }

  @Override
  public void clear(int index) {

  }

  @Override
  public void clear() {

  }

  @Override
  public @NotNull ListIterator<ItemStack> iterator() {
    return Collections.emptyListIterator();
  }

  @Override
  public @NotNull ListIterator<ItemStack> iterator(int index) {
    return Collections.emptyListIterator();
  }

}
