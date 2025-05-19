package com.lishid.openinv.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public final class SearchHelper {

  public static boolean findMatch(@NotNull Inventory inventory, @NotNull Predicate<@NotNull ItemStack> predicate) {
    for (ItemStack content : inventory.getContents()) {
      if (findMatch(content, predicate)) {
        return true;
      }
    }
    return false;
  }

  private static boolean findMatch(@Nullable ItemStack itemStack, @NotNull Predicate<@NotNull ItemStack> predicate) {
    if (itemStack == null || itemStack.getType().isAir()) {
      return false;
    }

    // If the item is the search target, done.
    if (predicate.test(itemStack)) {
      return true;
    }

    // If the item doesn't have meta, it cannot contain items.
    if (!itemStack.hasItemMeta()) {
      return false;
    }

    ItemMeta meta = itemStack.getItemMeta();

    // Container meta with items (primarily shulkers).
    if (meta instanceof BlockStateMeta stateMeta) {
      if (!stateMeta.hasBlockState() || !(stateMeta.getBlockState() instanceof InventoryHolder holder)) {
        return false;
      }
      Inventory inventory = holder.getInventory();
      return findMatch(inventory, predicate);
    }

    // Bundle meta.
    if (meta instanceof BundleMeta bundleMeta) {
      for (ItemStack subStack : bundleMeta.getItems()) {
        if (findMatch(subStack, predicate)) {
          return true;
        }
      }
    }

    return false;
  }

  private SearchHelper() {
    throw new IllegalStateException("Cannot create instance of utility class.");
  }
}
