package com.lishid.openinv.internal.common.container.slot;

import com.lishid.openinv.internal.common.container.slot.placeholder.Placeholders;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A view-only {@link Slot}. "Blank" by default, but can wrap another slot to display its content.
 */
public class SlotViewOnly extends SlotPlaceholder {

  public static @NotNull SlotViewOnly wrap(@NotNull Slot wrapped) {
    SlotViewOnly wrapper;
    if (wrapped instanceof SlotPlaceholder placeholder) {
      wrapper = new SlotViewOnly(wrapped.container, wrapped.slot, wrapped.x, wrapped.y) {
        @Override
        public ItemStack getOrDefault() {
          return placeholder.getOrDefault();
        }
      };
    } else {
      wrapper = new SlotViewOnly(wrapped.container, wrapped.slot, wrapped.x, wrapped.y) {
        @Override
        public ItemStack getOrDefault() {
          return wrapped.getItem();
        }
      };
    }
    wrapper.index = wrapped.index;
    return wrapper;
  }

  public SlotViewOnly(Container container, int index, int x, int y) {
    super(container, index, x, y);
  }

  @Override
  public ItemStack getOrDefault() {
    return Placeholders.notSlot;
  }

  @Override
  public void onQuickCraft(@NotNull ItemStack itemStack1, @NotNull ItemStack itemStack2) {
  }

  @Override
  public void onTake(@NotNull Player player, @NotNull ItemStack itemStack) {
  }

  @Override
  public boolean mayPlace(@NotNull ItemStack itemStack) {
    return false;
  }

  @Override
  public @NotNull ItemStack getItem() {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean hasItem() {
    return false;
  }

  @Override
  public void setByPlayer(@NotNull ItemStack newStack) {
  }

  @Override
  public void setByPlayer(@NotNull ItemStack newStack, @NotNull ItemStack oldStack) {
  }

  @Override
  public void set(@NotNull ItemStack itemStack) {
  }

  @Override
  public void setChanged() {
  }

  @Override
  public int getMaxStackSize() {
    return 0;
  }

  @Override
  public int getMaxStackSize(@NotNull ItemStack itemStack) {
    return 0;
  }

  @Override
  public @NotNull ItemStack remove(int amount) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean mayPickup(@NotNull Player player) {
    return false;
  }

  @Override
  public boolean isActive() {
    return false;
  }

  @Override
  public @NotNull Optional<ItemStack> tryRemove(int var0, int var1, @NotNull Player player) {
    return Optional.empty();
  }

  @Override
  public @NotNull ItemStack safeTake(int var0, int var1, @NotNull Player player) {
    return ItemStack.EMPTY;
  }

  @Override
  public @NotNull ItemStack safeInsert(@NotNull ItemStack itemStack) {
    return itemStack;
  }

  @Override
  public @NotNull ItemStack safeInsert(@NotNull ItemStack itemStack, int amount) {
    return itemStack;
  }

  @Override
  public boolean allowModification(@NotNull Player player) {
    return false;
  }

  @Override
  public int getContainerSlot() {
    return this.slot;
  }

  @Override
  public boolean isHighlightable() {
    return false;
  }

  @Override
  public boolean isFake() {
    return true;
  }

}
