package com.github.jikoo.openinv.internal.spigot26_3.container.bukkit;

import com.github.jikoo.openinv.internal.spigot26_3.container.OpenInventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class OpenPlayerInventorySelf extends OpenPlayerInventory {

  private final int offset;

  public OpenPlayerInventorySelf(OpenInventory inventory, int offset) {
    super(inventory);
    this.offset = offset;
  }

  @Override
  public @Nullable ItemStack getItem(int index) {
    return super.getItem(offset + index);
  }

  @Override
  public void setItem(int index, @Nullable ItemStack item) {
    super.setItem(offset + index, item);
  }

}
