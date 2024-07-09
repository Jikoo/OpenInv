package com.lishid.openinv.internal;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InventoryAccessor {

  <T extends ISpecialInventory> @Nullable T get(Class<T> clazz, @NotNull Inventory bukkitInventory);

}
