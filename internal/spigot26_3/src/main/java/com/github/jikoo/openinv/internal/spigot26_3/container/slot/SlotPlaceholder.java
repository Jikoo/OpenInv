package com.github.jikoo.openinv.internal.spigot26_3.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * An implementation of a slot as used by a menu that may have fake placeholder items.
 *
 * <p>Used to prevent plugins (particularly sorting plugins) from adding placeholders to inventories.</p>
 */
@NullMarked
public abstract class SlotPlaceholder extends Slot {

  SlotPlaceholder(Container container, int index, int x, int y) {
    super(container, index, x, y);
  }

  public abstract ItemStack getOrDefault();

}
