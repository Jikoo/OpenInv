package com.github.jikoo.openinv.internal.spigot26_2.container.slot;

import com.github.jikoo.openinv.internal.container.slot.Content;
import com.github.jikoo.openinv.internal.spigot26_3.container.slot.ContentCrafting;
import com.github.jikoo.openinv.internal.spigot26_3.container.slot.ContentCursor;
import com.github.jikoo.openinv.internal.spigot26_3.container.slot.ContentDrop;
import com.github.jikoo.openinv.internal.spigot26_3.container.slot.ContentViewOnly;
import com.github.jikoo.openinv.internal.spigot26_3.container.slot.SlotViewOnly;
import com.github.jikoo.openinv.internal.spigot26_3.container.slot.placeholder.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenInventoryFactory extends com.github.jikoo.openinv.internal.spigot26_3.container.slot.OpenInventoryFactory {

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newCrafting(
      ServerPlayer holder,
      int index
  ) {
    return new ContentCrafting(holder, index) {
      @Override
      public void set(ItemStack itemStack) {
        if (isAvailable(holder)) {
          items.set(index, itemStack);
          holder.inventoryMenu.slotsChanged(holder.inventoryMenu.getCraftSlots());
        } else {
          this.holder.drop(itemStack, false);
        }
      }
    };
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newCursor(ServerPlayer holder) {
    return new ContentCursor(holder) {
      @Override
      public void set(ItemStack itemStack) {
        if (isAvailable()) {
          holder.containerMenu.setCarried(itemStack);
        } else {
          this.holder.drop(itemStack, false);
        }
      }
    };
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newDrop(ServerPlayer holder) {
    return new ContentDrop(holder) {
      @Override
      public void set(ItemStack itemStack) {
        this.holder.drop(itemStack, false);
      }
    };
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newViewOnly(ServerPlayer holder) {
    return newViewOnly(holder, Placeholders.notSlot);
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newViewOnly(
      ServerPlayer holder,
      ItemStack itemStack
  ) {
    return new ContentViewOnly(holder) {
      @Override
      public void set(ItemStack itemStack) {
        this.holder.drop(itemStack, false);
      }

      @Override
      public Slot asSlot(Container container, int slot, int x, int y) {
        return new SlotViewOnly(container, slot, x, y) {
          @Override
          public ItemStack getOrDefault() {
            return itemStack;
          }
        };
      }
    };
  }

}
