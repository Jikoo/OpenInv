package com.lishid.openinv.internal.paper1_21_4.container;

import com.lishid.openinv.internal.common.container.slot.ContentCrafting;
import com.lishid.openinv.internal.common.container.slot.ContentCursor;
import com.lishid.openinv.internal.common.container.slot.ContentDrop;
import com.lishid.openinv.internal.common.container.slot.ContentList;
import com.lishid.openinv.internal.common.container.slot.ContentViewOnly;
import com.lishid.openinv.internal.common.container.slot.SlotViewOnly;
import com.lishid.openinv.internal.common.container.slot.placeholder.Placeholders;
import com.lishid.openinv.internal.paper1_21_4.container.menu.OpenInventoryMenu;
import com.lishid.openinv.internal.paper1_21_4.container.slot.ContentEquipment;
import com.lishid.openinv.internal.paper1_21_4.container.slot.ContentOffHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenInventory extends com.lishid.openinv.internal.common.container.OpenInventory {

  public OpenInventory(@NotNull org.bukkit.entity.Player bukkitPlayer) {
    super(bukkitPlayer);
  }

  @Override
  protected void setupSlots() {
    // Top of inventory: Regular contents.
    int nextIndex = addMainInventory();

    // If inventory is expected size, we can arrange slots to be pretty.
    Inventory ownerInv = owner.getInventory();
    if (ownerInv.items.size() == 36
        && ownerInv.armor.size() == 4
        && ownerInv.offhand.size() == 1
        && owner.inventoryMenu.getCraftSlots().getContainerSize() == 4) {
      // Armor slots: Bottom left.
      addArmor(36);
      // Off-hand: Below chestplate.
      addOffHand(46);
      // Drop slot: Bottom right.
      slots.set(53, new ContentDrop(owner));
      // Cursor slot: Above drop.
      slots.set(44, new ContentCursor(owner));

      // Crafting is displayed in the bottom right corner.
      // As we're using the pretty view, this is a 3x2.
      addCrafting(41, true);
      return;
    }

    // Otherwise we'll just add elements linearly.
    nextIndex = addArmor(nextIndex);
    nextIndex = addOffHand(nextIndex);
    nextIndex = addCrafting(nextIndex, false);
    slots.set(nextIndex, new ContentCursor(owner));
    // Drop slot last.
    slots.set(slots.size() - 1, new ContentDrop(owner));
  }

  private int addMainInventory() {
    int listSize = owner.getInventory().items.size();
    // Hotbar slots are 0-8. We want those to appear on the bottom of the inventory like a normal player inventory,
    // so everything else needs to move up a row.
    int hotbarDiff = listSize - 9;
    for (int localIndex = 0; localIndex < listSize; ++localIndex) {
      InventoryType.SlotType type;
      int invIndex;
      if (localIndex < hotbarDiff) {
        invIndex = localIndex + 9;
        type = InventoryType.SlotType.CONTAINER;
      } else {
        type = InventoryType.SlotType.QUICKBAR;
        invIndex = localIndex - hotbarDiff;
      }

      slots.set(localIndex, new ContentList(owner, invIndex, type) {
        @Override
        public void setHolder(@NotNull ServerPlayer holder) {
          items = holder.getInventory().items;
        }
      });
    }
    return listSize;
  }

  private int addArmor(int startIndex) {
    int listSize = owner.getInventory().armor.size();

    for (int i = 0; i < listSize; ++i) {
      // Armor slots go bottom to top; boots are slot 0, helmet is slot 3.
      // Since we have to display horizontally due to space restrictions,
      // making the left side the "top" is more user-friendly.
      int armorIndex;
      EquipmentSlot slot;
      switch (i) {
        case 3 -> {
          armorIndex = 0;
          slot = EquipmentSlot.FEET;
        }
        case 2 -> {
          armorIndex = 1;
          slot = EquipmentSlot.LEGS;
        }
        case 1 -> {
          armorIndex = 2;
          slot = EquipmentSlot.CHEST;
        }
        case 0 -> {
          armorIndex = 3;
          slot = EquipmentSlot.HEAD;
        }
        default -> {
          // In the event that new armor slots are added, they can be placed at the end.
          armorIndex = i;
          slot = EquipmentSlot.MAINHAND;
        }
      }

      slots.set(startIndex + i, new ContentEquipment(owner, armorIndex, slot));
    }

    return startIndex + listSize;
  }

  private int addOffHand(int startIndex) {
    int listSize = owner.getInventory().offhand.size();
    for (int localIndex = 0; localIndex < listSize; ++localIndex) {
      slots.set(startIndex + localIndex, new ContentOffHand(owner, localIndex));
    }
    return startIndex + listSize;
  }

  private int addCrafting(int startIndex, boolean pretty) {
    int listSize = owner.inventoryMenu.getCraftSlots().getContents().size();
    pretty &= listSize == 4;

    for (int localIndex = 0; localIndex < listSize; ++localIndex) {
      // Pretty display is a 2x2 rather than linear.
      // If index is in top row, grid is not 2x2, or pretty is disabled, just use current index.
      // Otherwise, subtract 2 and add 9 to start in the same position on the next row.
      int modIndex = startIndex + (localIndex < 2 || !pretty ? localIndex : localIndex + 7);

      slots.set(modIndex, new ContentCrafting(owner, localIndex));
    }

    if (pretty) {
      slots.set(startIndex + 2, new ContentViewOnly(owner) {
        @Override
        public Slot asSlot(Container container, int slot, int x, int y) {
          return new SlotViewOnly(container, slot, x, y) {
            @Override
            public ItemStack getOrDefault() {
              return Placeholders.craftingOutput;
            }
          };
        }
      });
      slots.set(startIndex + 11, getCraftingResult(owner));
    }

    return startIndex + listSize;
  }

  public @Nullable AbstractContainerMenu createMenu(Player player, int i, boolean viewOnly) {
    if (player instanceof ServerPlayer serverPlayer) {
      return new OpenInventoryMenu(this, serverPlayer, i, viewOnly);
    }
    return null;
  }

}
