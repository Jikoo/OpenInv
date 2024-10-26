package com.lishid.openinv.internal.v1_21_R2.container.slot;

import com.lishid.openinv.internal.v1_21_R2.container.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * A fake slot used to drop items. Unavailable offline.
 */
public class ContentDrop implements Content {

  private ServerPlayer holder;

  public ContentDrop(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public ItemStack get() {
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack remove() {
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack removePartial(int amount) {
    return ItemStack.EMPTY;
  }

  @Override
  public void set(ItemStack itemStack) {
    holder.drop(itemStack, true);
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotDrop(container, slot, x, y);
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    // Behaves like dropping an item outside the screen, just by the target player.
    return InventoryType.SlotType.OUTSIDE;
  }

  public class SlotDrop extends SlotPlaceholder {

    private SlotDrop(Container container, int index, int x, int y) {
      super(container, index, x, y);
    }

    @Override
    public ItemStack getOrDefault() {
      return holder.connection != null && !holder.connection.isDisconnected()
          ? Placeholders.drop
          : Placeholders.blockedOffline;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
      return holder.connection != null && !holder.connection.isDisconnected();
    }

    @Override
    public boolean hasItem() {
      return false;
    }

    @Override
    public boolean isFake() {
      return true;
    }

  }

}
