package com.lishid.openinv.internal.paper26_3.container.slot;

import com.lishid.openinv.internal.paper26_3.container.slot.placeholder.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NullMarked;

/**
 * A slot allowing viewing of the crafting result.
 *
 * <p>Unmodifiable because I said so. Use your own crafting grid.</p>
 */
@NullMarked
class ContentCraftingResult extends ContentViewOnly {

  ContentCraftingResult(ServerPlayer holder) {
    super(holder);
  }

  @Override
  public ItemStack get() {
    InventoryMenu inventoryMenu = holder.inventoryMenu;
    return inventoryMenu.getResultSlot().getItem();
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotViewOnly(container, slot, x, y) {
      @Override
      public ItemStack getOrDefault() {
        if (!ContentCrafting.isAvailable(holder)) {
          return Placeholders.survivalOnly(holder);
        }
        InventoryMenu inventoryMenu = holder.inventoryMenu;
        return inventoryMenu.getResultSlot().getItem();
      }
    };
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return InventoryType.SlotType.RESULT;
  }

}
