package com.lishid.openinv.internal.paper26_3.container.slot;

import com.lishid.openinv.internal.paper26_3.container.slot.placeholder.Placeholders;
import com.lishid.openinv.internal.paper26_3.player.OpenPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

/**
 * A fake slot used to drop items. Unavailable offline.
 */
public class ContentDrop implements Content {

  static final BiConsumer<ServerPlayer, ItemStack> DROP;

  // TODO Content factory
  static {
    BiConsumer<ServerPlayer, ItemStack> dropMethod = null;
    try {
      Class.forName("net.minecraft.util.Prediction");
      dropMethod = (holder, itemStack) -> holder.drop(itemStack, false, Prediction.SERVER_ONLY);
    } catch (ClassNotFoundException e) {
      try {
        Method method = ServerPlayer.class.getMethod("drop", ItemStack.class, boolean.class);
        dropMethod = (holder, itemStack) -> {
          try {
            method.invoke(holder, itemStack, false);
          } catch (IllegalAccessException | InvocationTargetException ex) {
            // Shouldn't be possible. Eat the item until I write a slot factory.
          }
        };
      } catch (NoSuchMethodException ex) {
        // As above, eat for now.
        dropMethod = (holder, itemStack) -> {};
      }
    }
    DROP = dropMethod;
  }

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
    DROP.accept(this.holder, itemStack);
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
    public boolean mayPlace(@NotNull ItemStack itemStack) {
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
