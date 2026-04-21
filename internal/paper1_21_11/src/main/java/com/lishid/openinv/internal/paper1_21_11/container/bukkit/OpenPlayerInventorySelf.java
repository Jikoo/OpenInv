package com.lishid.openinv.internal.paper1_21_11.container.bukkit;

import com.lishid.openinv.internal.paper1_21_11.container.BaseOpenInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OpenPlayerInventorySelf extends OpenPlayerInventory {

  private final int offset;

  public OpenPlayerInventorySelf(@NotNull BaseOpenInventory inventory, int offset) {
    super(inventory);
    this.offset = offset;
  }

  @Override
  public ItemStack getItem(int index) {
    return super.getItem(offset + index);
  }

  @Override
  public void setItem(int index, ItemStack item) {
    super.setItem(offset + index, item);
  }

}
