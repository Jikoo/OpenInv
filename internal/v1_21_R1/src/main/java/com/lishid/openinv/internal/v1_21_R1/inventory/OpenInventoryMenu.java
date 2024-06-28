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
      // Default to 27-slot inventory.
      default -> MenuType.GENERIC_9x3;
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

  /**
   * Reimplementation of {@link AbstractContainerMenu#moveItemStackTo(ItemStack, int, int, boolean)} that ignores fake
   * slots and respects {@link Slot#hasItem()}.
   * 
   * @param itemStack the stack to quick-move
   * @param rangeLow the start of the range of slots that can be moved to, inclusive
   * @param rangeHigh the end of the range of slots that can be moved to, exclusive
   * @param topDown whether to start at the top of the range or bottom
   * @return whether the stack was modified as a result of being quick-moved
   */
  protected boolean moveItemStackTo(ItemStack itemStack, int rangeLow, int rangeHigh, boolean topDown) {
    boolean modified = false;

    for (int index = topDown ? rangeHigh - 1 : rangeLow;
         !itemStack.isEmpty() && topDown ? index >= rangeLow : index < rangeHigh;
         index += topDown ? -1 : 1
    ) {
      Slot slot = slots.get(index);
      if (slot.isFake() || !slot.mayPlace(itemStack)) {
        continue;
      }
      
      if (slot.hasItem()) {
        modified = addToExistingStack(itemStack, slot);
      } else  {
        // If there's no item here, add as many as we can of the item.
        slot.setByPlayer(itemStack.split(Math.min(itemStack.getCount(), slot.getMaxStackSize(itemStack))));
        slot.setChanged();
        modified = true;
        break;
      }
    }

    return modified;
  }

  private static boolean addToExistingStack(ItemStack itemStack, Slot slot) {
    // If there's an item here, unstackable items can't be added.
    if (!itemStack.isStackable()) {
      return false;
    }

    ItemStack existing = slot.getItem();

    // If the items aren't the same, we can't add our item.
    if (!ItemStack.isSameItemSameComponents(itemStack, existing)) {
      return false;
    }

    int total = existing.getCount() + itemStack.getCount();
    int max = slot.getMaxStackSize(existing);

    // If the existing item can accept the entirety of our item, we're done!
    if (total <= max) {
      itemStack.setCount(0);
      existing.setCount(total);
      slot.setChanged();
      return true;
    }

    // Otherwise, add as many as we can.
    itemStack.shrink(max - existing.getCount());
    existing.setCount(max);
    slot.setChanged();
    return true;
  }

  @Override
  public boolean canDragTo(Slot slot) {
    return !(slot instanceof ContainerSlotDrop.SlotDrop || slot instanceof ContainerSlotEmpty.SlotEmpty);
  }

}
