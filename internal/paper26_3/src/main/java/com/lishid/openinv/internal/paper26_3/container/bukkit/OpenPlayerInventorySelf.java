package com.lishid.openinv.internal.paper26_3.container.bukkit;

import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import com.google.common.base.Preconditions;
import com.lishid.openinv.internal.paper26_3.container.BaseOpenInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;

@NullMarked
public class OpenPlayerInventorySelf extends OpenPlayerInventory {

  private final int offset;

  public OpenPlayerInventorySelf(
      InventoryFactory<ServerPlayer, net.minecraft.world.item.ItemStack, Container, Slot, EquipmentSlot> factory,
      BaseOpenInventory inventory,
      int offset
  ) {
    super(factory, inventory);
    this.offset = offset;
  }

  @Override
  public int getSize() {
    return super.getSize() - offset;
  }

  @Override
  public @Nullable ItemStack getItem(int index) {
    return super.getItem(offset + index);
  }

  @Override
  public @Nullable ItemStack[] getStorageContents() {
    return new ItemStack[0];
  }

  @Override
  public void setStorageContents(@Nullable ItemStack[] items) throws IllegalArgumentException {
    Preconditions.checkArgument(items.length == 0, "items.length must be <= 0");
  }

  @Override
  public @Nullable ItemStack[] getContents() {
    @Nullable ItemStack[] contents = new ItemStack[getSize()];
    for (int i = 0; i < contents.length; ++i) {
      contents[i] = getItem(i);
    }
    return contents;
  }

  @Override
  public void setContents(@Nullable ItemStack[] items) {
    int size = getSize();
    Preconditions.checkArgument(items.length <= size, "items.length must be <= %s", size);

    for (int index = 0; index < size; ++index) {
      ItemStack itemStack = items[index];
      if (itemStack == null || itemStack.isEmpty()) {
        continue;
      }
      validateSet(index);
    }

    for (int index = 0; index < size; ++index) {
      if (index < items.length) {
        inventory.setItem(offset + index, CraftItemStack.asNMSCopy(items[index]));
      } else {
        inventory.setItem(offset + index, net.minecraft.world.item.ItemStack.EMPTY);
      }
    }
  }

  @Override
  public void setItem(int index, @Nullable ItemStack item) {
    super.setItem(offset + index, item);
  }

  @Override
  public boolean contains(Material material) {
    // Only checks the storage contents.
    return false;
  }

  @Override
  public boolean contains(ItemStack item) {
    return false;
  }

  @Override
  public boolean contains(Material material, int amount) {
    return false;
  }

  @Override
  public boolean contains(ItemStack item, int amount) {
    return false;
  }

  @Override
  public boolean containsAtLeast(ItemStack item, int amount) {
    return false;
  }

  @SuppressWarnings("NonApiType")
  @Override
  public HashMap<Integer, ItemStack> all(Material material) {
    // No storage contents, no results.
    return new HashMap<>();
  }

  @SuppressWarnings("NonApiType")
  @Override
  public HashMap<Integer, ItemStack> all(@Nullable ItemStack item) {
    return new HashMap<>();
  }

  @Override
  public int first(Material material) {
    return -1;
  }

  @Override
  public int first(ItemStack item) {
    return -1;
  }

  @Override
  public int firstEmpty() {
    return -1;
  }

  @SuppressWarnings("NonApiType")
  @Override
  public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
    HashMap<Integer, ItemStack> map = new HashMap<>();
    for (int index = 0; index < items.length; ++index) {
      map.put(index, items[index]);
    }
    return map;
  }

  @Override
  public HashMap<Integer, ItemStack> removeItem(ItemStack... items) {
    return addItem(items);
  }

  @Override
  public void remove(Material material) {
    Preconditions.checkNotNull(material, "Material cannot be null");
    Preconditions.checkArgument(material.isItem(), "Material must be an item");
  }

  @Override
  public void remove(ItemStack itemStack) {
    // Sure did.
  }

}
