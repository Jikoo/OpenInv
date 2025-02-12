package com.lishid.openinv.internal.paper1_21_1.container.slot;

import com.lishid.openinv.internal.common.container.slot.ContentCrafting;
import com.lishid.openinv.internal.common.container.slot.ContentViewOnly;
import com.lishid.openinv.internal.common.container.slot.SlotViewOnly;
import com.lishid.openinv.internal.common.container.slot.placeholder.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class ContentCraftingResult extends ContentViewOnly {

  public ContentCraftingResult(@NotNull ServerPlayer holder) {
    super(holder);
  }

  @Override
  public ItemStack get() {
    InventoryMenu inventoryMenu = holder.inventoryMenu;
    return inventoryMenu.getSlot(inventoryMenu.getResultSlotIndex()).getItem();
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
        return inventoryMenu.getSlot(inventoryMenu.getResultSlotIndex()).getItem();
      }
    };
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return InventoryType.SlotType.RESULT;
  }

}
