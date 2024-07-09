package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * A slot allowing viewing of the crafting result.
 *
 * <p>Unmodifiable because I said so. Use your own crafting grid.</p>
 */
class ContainerSlotCraftingResult extends ContainerSlotUninteractable {

  ContainerSlotCraftingResult(@NotNull ServerPlayer holder) {
    super(holder);
  }

  @Override
  public ItemStack get() {
    InventoryMenu inventoryMenu = holder.inventoryMenu;
    return inventoryMenu.getSlot(inventoryMenu.getResultSlotIndex()).getItem();
  }

  @Override
  public Slot asMenuSlot(Container container, int index, int x, int y) {
    return new SlotUninteractable(container, index, x, y) {
      @Override
      ItemStack getOrDefault() {
        if (!ContainerSlotCrafting.isAvailable(holder)) {
          return Placeholders.survivalOnly(holder);
        }
        InventoryMenu inventoryMenu = holder.inventoryMenu;
        return inventoryMenu.getSlot(inventoryMenu.getResultSlotIndex()).getItem();
      }
    };
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return InventoryType.SlotType.RESULT;
  }

}
