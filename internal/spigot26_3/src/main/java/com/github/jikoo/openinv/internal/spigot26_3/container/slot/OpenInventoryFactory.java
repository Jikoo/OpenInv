package com.github.jikoo.openinv.internal.spigot26_3.container.slot;

import com.github.jikoo.openinv.internal.container.slot.Content;
import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Function;

@NullMarked
public class OpenInventoryFactory implements InventoryFactory<ServerPlayer, ItemStack, Container, Slot, EquipmentSlot> {

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newCrafting(ServerPlayer holder, int index) {
    return new ContentCrafting(holder, index);
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newCraftingResult(ServerPlayer holder) {
    return new ContentCraftingResult(holder);
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newCursor(ServerPlayer holder) {
    return new ContentCursor(holder);
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newDrop(ServerPlayer holder) {
    return new ContentDrop(holder);
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newEquipment(
      ServerPlayer holder,
      EquipmentSlot equipmentSlot
  ) {
    return new ContentEquipment(holder, equipmentSlot);
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newList(
      ServerPlayer holder,
      int index,
      InventoryType.SlotType slotType,
      Function<ServerPlayer, List<ItemStack>> listSupplier
  ) {
    return new ContentList(holder, index, slotType) {
      @Override
      protected List<ItemStack> getItems(ServerPlayer holder) {
        return listSupplier.apply(holder);
      }
    };
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newOffHand(ServerPlayer holder) {
    return new ContentOffHand(holder);
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newViewOnly(ServerPlayer holder) {
    return new ContentViewOnly(holder);
  }

  @Override
  public Content<ServerPlayer, ItemStack, Container, Slot> newViewOnly(ServerPlayer holder, ItemStack itemStack) {
    return new ContentViewOnly(holder) {
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

  @Override
  public Slot wrapViewOnly(Slot wrapped) {
    SlotViewOnly wrapper;
    if (wrapped instanceof SlotPlaceholder placeholder) {
      wrapper = new SlotViewOnly(wrapped.container, wrapped.getContainerSlot(), wrapped.x, wrapped.y) {
        @Override
        public ItemStack getOrDefault() {
          return placeholder.getOrDefault();
        }
      };
    } else {
      wrapper = new SlotViewOnly(wrapped.container, wrapped.getContainerSlot(), wrapped.x, wrapped.y) {
        @Override
        public ItemStack getOrDefault() {
          return wrapped.getItem();
        }
      };
    }
    wrapper.index = wrapped.index;
    return wrapper;
  }

  @Override
  public org.bukkit.inventory.ItemStack asBukkitMirror(ItemStack itemStack) {
    return CraftItemStack.asCraftMirror(itemStack);
  }

}
