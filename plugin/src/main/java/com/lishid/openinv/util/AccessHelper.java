package com.lishid.openinv.util;

import com.lishid.openinv.internal.ISpecialInventory;
import org.bukkit.inventory.Inventory;

import java.util.function.BiFunction;

public final class AccessHelper {

  public static void setProvider(BiFunction<Inventory, Class<? extends ISpecialInventory>, ISpecialInventory> provider) {
    InventoryAccess.setProvider(provider);
  }

  private AccessHelper() {
    throw new IllegalStateException("Cannot create instance of utility class.");
  }

}
