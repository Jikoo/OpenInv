package com.lishid.openinv.internal.v1_21_R2.container.slot;

import com.lishid.openinv.internal.v1_21_R2.container.Placeholders;
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
  public void onQuickCraft(ItemStack var0, ItemStack var1) {
  }

  @Override
  public void onTake(Player var0, ItemStack var1) {
  }

  @Override
  public boolean mayPlace(ItemStack var0) {
    return false;
  }

  @Override
  public ItemStack getItem() {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean hasItem() {
    return false;
  }

  @Override
  public void setByPlayer(ItemStack newStack) {
  }

  @Override
  public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
  }

  @Override
  public void set(ItemStack var0) {
  }

  @Override
  public void setChanged() {
  }

  @Override
  public int getMaxStackSize() {
    return 0;
  }

  @Override
  public int getMaxStackSize(ItemStack itemStack) {
    return 0;
  }

  @Override
  public ItemStack remove(int amount) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean mayPickup(Player var0) {
    return false;
  }

  @Override
  public boolean isActive() {
    return false;
  }

  @Override
  public Optional<ItemStack> tryRemove(int var0, int var1, Player var2) {
    return Optional.empty();
  }

  @Override
  public ItemStack safeTake(int var0, int var1, Player var2) {
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack safeInsert(ItemStack itemStack) {
    return itemStack;
  }

  @Override
  public ItemStack safeInsert(ItemStack itemStack, int amount) {
    return itemStack;
  }

  @Override
  public boolean allowModification(Player var0) {
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
