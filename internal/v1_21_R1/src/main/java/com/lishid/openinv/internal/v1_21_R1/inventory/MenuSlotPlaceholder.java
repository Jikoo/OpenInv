package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * An implementation of a slot as used by a menu that may have fake placeholder items.
 *
 * <p>Used to prevent plugins (particularly sorting plugins) from adding placeholders to inventories.</p>
 */
abstract class MenuSlotPlaceholder extends Slot {

  MenuSlotPlaceholder(Container container, int index, int x, int y) {
    super(container, index, x, y);
  }

  abstract ItemStack getOrDefault();

}
