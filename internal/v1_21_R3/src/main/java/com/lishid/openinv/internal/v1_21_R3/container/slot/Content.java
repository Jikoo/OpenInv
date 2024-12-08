package com.lishid.openinv.internal.v1_21_R3.container.slot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An interface defining behaviors for entries in a {@link Container}. Used to reduce duplicate content reordering.
 */
public interface Content {

  /**
   * Update internal holder.
   *
   * @param holder the new holder
   */
  void setHolder(@NotNull ServerPlayer holder);

  /**
   * Get the current item.
   *
   * @return the current item
   */
  ItemStack get();

  /**
   * Remove the current item.
   *
   * @return the current item
   */
  ItemStack remove();

  /**
   * Remove some of the current item.
   *
   * @return the current item
   */
  ItemStack removePartial(int amount);

  /**
   * Set the current item. If slot is currently not usable, will drop item instead.
   *
   * @param itemStack the item to set
   */
  void set(ItemStack itemStack);

  /**
   * Get a {@link Slot} for use in a {@link net.minecraft.world.inventory.AbstractContainerMenu ContainerMenu}. Will
   * impose any specific restrictions to insertion or removal.
   *
   * @param container the backing container
   * @param slot the slot of the backing container represented
   * @param x clientside x dimension from top left of inventory, not used
   * @param y clientside y dimension from top left of inventory, not used
   * @return a menu slot
   */
  Slot asSlot(Container container, int slot, int x, int y);

  /**
   * Get a loose Bukkit translation of what this slot stores. For example, any slot that drops items at the owner rather
   * than insert them will report itself as being {@link org.bukkit.event.inventory.InventoryType.SlotType#OUTSIDE}.
   *
   * @return the closes Bukkit slot type
   */
  org.bukkit.event.inventory.InventoryType.SlotType getSlotType();

}
