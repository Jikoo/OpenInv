package com.github.jikoo.openinv.internal.spigot26_3.container.slot;

import com.github.jikoo.openinv.internal.container.slot.Content;
import com.github.jikoo.openinv.internal.spigot26_3.container.slot.placeholder.Placeholders;
import com.github.jikoo.openinv.internal.spigot26_3.player.OpenPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NullMarked;

/**
 * A fake slot used to drop items. Unavailable offline.
 */
@NullMarked
public class ContentDrop implements Content<ServerPlayer, ItemStack, Container, Slot> {

  protected ServerPlayer holder;

  protected ContentDrop(ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public void setHolder(ServerPlayer holder) {
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
    this.holder.drop(itemStack, false, Prediction.SERVER_ONLY);
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
      return OpenPlayer.isConnected(holder.connection)
          ? Placeholders.drop
          : Placeholders.blockedOffline;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
      return OpenPlayer.isConnected(holder.connection);
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
