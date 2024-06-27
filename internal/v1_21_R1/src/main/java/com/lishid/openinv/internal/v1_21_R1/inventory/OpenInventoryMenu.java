package com.lishid.openinv.internal.v1_21_R1.inventory;

import com.lishid.openinv.internal.InventoryViewTitle;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryView;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class OpenInventoryMenu extends AbstractContainerMenu {

  private final OpenInventory inventory;
  private final ServerPlayer viewer;
  private final int topSize;
  private final int offset;
  private CraftInventoryView bukkitEntity;

  protected OpenInventoryMenu(OpenInventory inventory, ServerPlayer viewer, int i) {
    super(getMenuType(inventory, viewer), i);
    this.inventory = inventory;
    this.viewer = viewer;

    int upperRows;
    boolean ownInv = inventory.getOwnerHandle().equals(viewer);
    if (ownInv) {
      // Disallow duplicate access to own main inventory contents.
      offset = viewer.getInventory().items.size();
      upperRows = ((int) Math.ceil((inventory.getContainerSize() - offset) / 9.0));
    } else {
      offset = 0;
      upperRows = inventory.getContainerSize() / 9;
    }

    // View's upper inventory - our container
    for (int row = 0; row < upperRows; ++row) {
      for (int col = 0; col < 9; ++col) {
        // x and y for client purposes, but hey, we're thorough here.
        // Adapted from net.minecraft.world.inventory.ChestMenu
        int x = 8 + col * 18;
        int y = 18 + row * 18;
        int index = offset + row * 9 + col;

        // Guard against weird inventory sizes.
        if (index >= inventory.getContainerSize()) {
          addSlot(new ContainerSlotEmpty.SlotEmpty(inventory, index, x, y));
          continue;
        }

        Slot slot = inventory.getMenuSlot(index, x, y);

        // Disallow access to cursor slot for own inventory - opens the door to a lot of ways to delete items.
        if (ownInv && slot instanceof ContainerSlotCursor.SlotCursor) {
          slot = new ContainerSlotEmpty.SlotEmpty(inventory, index, x, y);
        }
        addSlot(slot);
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

    this.topSize = slots.size() - 36;
  }

  static MenuType<?> getMenuType(OpenInventory inventory, ServerPlayer viewer) {
    int size = inventory.getContainerSize();
    if (inventory.getOwnerHandle().equals(viewer)) {
      size -= viewer.getInventory().items.size();
      size = ((int) Math.ceil(size / 9.0)) * 9;
    }

    return switch (size) {
      case 9 -> MenuType.GENERIC_9x1;
      case 18 -> MenuType.GENERIC_9x2;
      case 36 -> MenuType.GENERIC_9x4;
      case 45 -> MenuType.GENERIC_9x5;
      case 54 -> MenuType.GENERIC_9x6;
      default -> MenuType.GENERIC_9x3; // Default 27-slot inventory
    };
  }

  @Override
  public CraftInventoryView getBukkitView() {
    if (bukkitEntity == null) {
      bukkitEntity = new CraftInventoryView(viewer.getBukkitEntity(), new OpenPlayerInventory(inventory), this) {
        private String title;

        @Override
        public @NotNull String getOriginalTitle() {
          return InventoryViewTitle.PLAYER_INVENTORY.getTitle(viewer.getBukkitEntity(), inventory);
        }

        @Override
        public @NotNull String getTitle() {
          if (title == null) {
            title = getOriginalTitle();
          }
          return title;
        }

        @Override
        public void setTitle(String title) {
          CraftInventoryView.sendInventoryTitleChange(this, title);
          this.title = title;
        }

        @NotNull
        @Override
        public InventoryType.SlotType getSlotType(int slot) {
          if (slot < 0) {
            return InventoryType.SlotType.OUTSIDE;
          }
          if (slot >= topSize) {
            return super.getSlotType(offset + slot);
          }
          return inventory.getSlotType(offset + slot);
        }
      };
    }
    return bukkitEntity;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    // See net.minecraft.world.inventory.ChestMenu
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    if (slot.hasItem()) {
      ItemStack itemstack1 = slot.getItem();
      itemstack = itemstack1.copy();
      int topSize = slots.size() - 36;
      if (index < topSize) {
        if (!this.moveItemStackTo(itemstack1, topSize, this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(itemstack1, 0, topSize, false)) {
        // TODO review logic:
        //  - exclude drop slot
        //  - check if we need to do more work to ignore placeholders if added to normal slots
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.setByPlayer(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
    }

    return itemstack;
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

}
