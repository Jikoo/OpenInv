package com.github.jikoo.openinv.internal.spigot26_3.container.slot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * A normal slot backed by an item list.
 */
@NullMarked
public abstract class ContentList implements Content {

  private final int index;
  private final InventoryType.SlotType slotType;
  protected List<ItemStack> items;

  public ContentList(ServerPlayer holder, int index, InventoryType.SlotType slotType) {
    this.index = index;
    this.slotType = slotType;
    setHolder(holder);
  }

  @Override
  public void setHolder(ServerPlayer holder) {
    this.items = getItems(holder);
  }

  protected abstract List<ItemStack> getItems(ServerPlayer holder);

  @Override
  public ItemStack get() {
    return items.get(index);
  }

  @Override
  public ItemStack remove() {
    ItemStack removed = items.remove(index);
    return removed == null || removed.isEmpty() ? ItemStack.EMPTY : removed;
  }

  @Override
  public ItemStack removePartial(int amount) {
    return ContainerHelper.removeItem(items, index, amount);
  }

  @Override
  public void set(ItemStack itemStack) {
    items.set(index, itemStack);
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new Slot(container, slot, x, y);
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return slotType;
  }

}
