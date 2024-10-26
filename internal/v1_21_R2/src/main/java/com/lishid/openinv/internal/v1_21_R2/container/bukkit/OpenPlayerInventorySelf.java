package com.lishid.openinv.internal.v1_21_R2.container.bukkit;

import com.lishid.openinv.internal.v1_21_R2.container.OpenInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OpenPlayerInventorySelf extends OpenPlayerInventory {

  private final int offset;

  public OpenPlayerInventorySelf(@NotNull OpenInventory inventory, int offset) {
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
