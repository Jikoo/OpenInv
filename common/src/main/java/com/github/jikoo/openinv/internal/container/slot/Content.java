package com.github.jikoo.openinv.internal.container.slot;

import org.jspecify.annotations.NullMarked;

/**
 * An interface defining behaviors for entries in a {@link net.minecraft.world.Container Container}. Used to reduce duplicate content reordering.
 */
@NullMarked
public interface Content<P, I, C, S> {

  /**
   * Update internal holder.
   *
   * @param holder the new holder
   */
  void setHolder(P holder);

  /**
   * Get the current item.
   *
   * @return the current item
   */
  I get();

  /**
   * Remove the current item.
   *
   * @return the current item
   */
  I remove();

  /**
   * Remove some of the current item.
   *
   * @return the current item
   */
  I removePartial(int amount);

  /**
   * Set the current item. If slot is currently not usable, will drop item instead.
   *
   * @param itemStack the item to set
   */
  void set(I itemStack);

  /**
   * Get a {@link net.minecraft.world.inventory.Slot Slot} for use in a {@link net.minecraft.world.inventory.AbstractContainerMenu ContainerMenu}. Will
   * impose any specific restrictions to insertion or removal.
   *
   * @param container the backing container
   * @param slot the slot of the backing container represented
   * @param x clientside x dimension from top left of inventory, not used
   * @param y clientside y dimension from top left of inventory, not used
   * @return a menu slot
   */
  S asSlot(C container, int slot, int x, int y);

  /**
   * Get a loose Bukkit translation of what this slot stores. For example, any slot that drops items at the owner rather
   * than insert them will report itself as being {@link org.bukkit.event.inventory.InventoryType.SlotType#OUTSIDE}.
   *
   * @return the closes Bukkit slot type
   */
  org.bukkit.event.inventory.InventoryType.SlotType getSlotType();

}
