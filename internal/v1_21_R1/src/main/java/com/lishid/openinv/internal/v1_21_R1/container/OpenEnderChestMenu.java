package com.lishid.openinv.internal.v1_21_R1.container;

import com.lishid.openinv.internal.v1_21_R1.container.slot.SlotViewOnly;
import com.lishid.openinv.util.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class OpenEnderChestMenu extends OpenContainerMenu {

  private final OpenEnderChest enderChest;
  private final int topSize;
  private CraftInventoryView view;

  OpenEnderChestMenu(OpenEnderChest enderChest, ServerPlayer viewer, int containerId) {
    super(getMenuType(enderChest), containerId, enderChest.getOwnerHandle(), viewer);
    this.enderChest = enderChest;
    int upperRows = (int) Math.ceil(enderChest.getContainerSize() / 9.0);
    topSize = upperRows * 9;

    // View's upper inventory - our container
    for (int row = 0; row < upperRows; ++row) {
      for (int col = 0; col < 9; ++col) {
        // x and y for client purposes, but hey, we're thorough here.
        // Adapted from net.minecraft.world.inventory.ChestMenu
        int x = 8 + col * 18;
        int y = 18 + row * 18;
        int index = row * 9 + col;

        // Guard against weird inventory sizes.
        if (index >= enderChest.getContainerSize()) {
          addSlot(new SlotViewOnly(enderChest, index, x, y));
          continue;
        }

        addSlot(new Slot(enderChest, index, x, y));
      }
    }

    // View's lower inventory - viewer inventory
    int playerInvPad = (upperRows - 4) * 18;
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 9; ++col) {
        int x = 8 + col * 18;
        int y = playerInvPad + row * 18 + 103;
        addSlot(new Slot(viewer.getInventory(), row * 9 + col + 9, x, y));
      }
    }
    // Hotbar
    for (int col = 0; col < 9; ++col) {
      int x = 8 + col * 18;
      int y = playerInvPad + 161;
      addSlot(new Slot(viewer.getInventory(), col, x, y));
    }
  }

  private static MenuType<?> getMenuType(OpenEnderChest enderChest) {
    return OpenContainerMenu.getContainers(((int) Math.ceil(enderChest.getContainerSize() / 9.0)) * 9);
  }

  @Override
  protected boolean checkViewOnly() {
    return  !(ownContainer ? Permissions.ENDERCHEST_EDIT_SELF : Permissions.ENDERCHEST_OPEN_OTHER)
        .hasPermission(viewer.getBukkitEntity());
  }

  @Override
  public CraftInventoryView getBukkitView() {
    if (view == null) {
      Inventory top;
      if (viewOnly) {
        top = new OpenViewInventory(enderChest);
      } else {
        top = enderChest.getBukkitInventory();
      }
      view = new CraftInventoryView(viewer.getBukkitEntity(), top, this) {
        @Override
        public @Nullable Inventory getInventory(int rawSlot) {
          if (viewOnly) {
            return null;
          }
          return super.getInventory(rawSlot);
        }

        @Override
        public int convertSlot(int rawSlot) {
          if (viewOnly) {
            return InventoryView.OUTSIDE;
          }
          return super.convertSlot(rawSlot);
        }

        @Override
        public @NotNull InventoryType.SlotType getSlotType(int slot) {
          if (viewOnly) {
            return InventoryType.SlotType.OUTSIDE;
          }
          return super.getSlotType(slot);
        }
      };
    }
    return view;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    // See ChestMenu
    Slot slot = this.slots.get(index);

    if (slot.isFake() || !slot.hasItem()) {
      return ItemStack.EMPTY;
    }

    ItemStack itemStack = slot.getItem();
    ItemStack original = itemStack.copy();

    if (index < topSize) {
      if (!this.moveItemStackTo(itemStack, topSize, this.slots.size(), true)) {
        return ItemStack.EMPTY;
      }
    } else if (!this.moveItemStackTo(itemStack, 0, topSize, false)) {
      return ItemStack.EMPTY;
    }

    if (itemStack.isEmpty()) {
      slot.setByPlayer(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }

    return original;
  }

  @Override
  public boolean stillValid(Player entityhuman) {
    return true;
  }

}
