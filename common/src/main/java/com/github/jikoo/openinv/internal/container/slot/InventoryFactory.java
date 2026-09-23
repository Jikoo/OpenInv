package com.github.jikoo.openinv.internal.container.slot;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Function;

@NullMarked
public interface InventoryFactory<P, I, C, S, E> {

  Content<P, I, C, S> newCrafting(P holder, int index);

  Content<P, I, C, S> newCraftingResult(P holder);

  Content<P, I, C, S> newCursor(P holder);

  Content<P, I, C, S> newDrop(P holder);

  Content<P, I, C, S> newEquipment(P holder, E equipmentSlot);

  Content<P, I, C, S> newList(P holder, int index, InventoryType.SlotType slotType, Function<P, List<I>> listSupplier);

  Content<P, I, C, S> newOffHand(P holder);

  Content<P, I, C, S> newViewOnly(P holder);

  Content<P, I, C, S> newViewOnly(P holder, I itemStack);

  S wrapViewOnly(S wrapped);

  ItemStack asBukkitMirror(I itemStack);

}
