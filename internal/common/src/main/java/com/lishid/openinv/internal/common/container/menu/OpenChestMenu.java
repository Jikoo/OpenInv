package com.lishid.openinv.internal.common.container.menu;

import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.InternalOwned;
import com.lishid.openinv.internal.common.container.bukkit.OpenDummyInventory;
import com.lishid.openinv.internal.common.container.slot.SlotViewOnly;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An extension of {@link AbstractContainerMenu} storing and managing data common to all special inventories.
 */
public abstract class OpenChestMenu<T extends Container & ISpecialInventory & InternalOwned<ServerPlayer>>
    extends AbstractContainerMenu {

  protected static final int BOTTOM_INVENTORY_SIZE = 36;

  protected final T container;
  protected final ServerPlayer viewer;
  protected final boolean viewOnly;
  protected final boolean ownContainer;
  protected final int topSize;
  private CraftInventoryView<OpenChestMenu<T>, Inventory> bukkitEntity;

  protected OpenChestMenu(
      @NotNull MenuType<ChestMenu> type,
      int containerCounter,
      @NotNull T container,
      @NotNull ServerPlayer viewer,
      boolean viewOnly
  ) {
    super(type, containerCounter);
    this.container = container;
    this.viewer = viewer;
    this.viewOnly = viewOnly;
    ownContainer = container.getOwnerHandle().equals(viewer);
    topSize = getTopSize(viewer);

    preSlotSetup();

    int upperRows = topSize / 9;
    // View's upper inventory - our container
    for (int row = 0; row < upperRows; ++row) {
      for (int col = 0; col < 9; ++col) {
        // x and y for client purposes, but hey, we're thorough here.
        // Adapted from net.minecraft.world.inventory.ChestMenu
        int x = 8 + col * 18;
        int y = 18 + row * 18;
        int index = row * 9 + col;

        // Guard against weird inventory sizes.
        if (index >= container.getContainerSize()) {
          addSlot(new SlotViewOnly(container, index, x, y));
          continue;
        }

        Slot slot = getUpperSlot(index, x, y);

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
  }

  public static @NotNull MenuType<ChestMenu> getChestMenuType(int inventorySize) {
    inventorySize = ((int) Math.ceil(inventorySize / 9.0)) * 9;
    return switch (inventorySize) {
      case 9 -> MenuType.GENERIC_9x1;
      case 18 -> MenuType.GENERIC_9x2;
      case 27 -> MenuType.GENERIC_9x3;
      case 36 -> MenuType.GENERIC_9x4;
      case 45 -> MenuType.GENERIC_9x5;
      case 54 -> MenuType.GENERIC_9x6;
      default -> throw new IllegalArgumentException("Inventory size unsupported: " + inventorySize);
    };
  }


  protected void preSlotSetup() {
  }

  protected @NotNull Slot getUpperSlot(int index, int x, int y) {
    Slot slot = new Slot(container, index, x, y);
    if (viewOnly) {
      return SlotViewOnly.wrap(slot);
    }
    return slot;
  }

  public boolean isViewOnly() {
    return viewOnly;
  }

  @Override
  public final @NotNull CraftInventoryView<OpenChestMenu<T>, Inventory> getBukkitView() {
    if (bukkitEntity == null) {
      bukkitEntity = createBukkitEntity();
    }

    return bukkitEntity;
  }

  protected @NotNull CraftInventoryView<OpenChestMenu<T>, Inventory> createBukkitEntity() {
    Inventory top;
    if (viewOnly) {
      top = new OpenDummyInventory(container, container.getBukkitType());
    } else {
      top = container.getBukkitInventory();
    }
    return new CraftInventoryView<>(viewer.getBukkitEntity(), top, this) {
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

  private int getTopSize(ServerPlayer viewer) {
    MenuType<?> menuType = getType();
    if (menuType == MenuType.GENERIC_9x1) {
      return 9;
    } else if (menuType == MenuType.GENERIC_9x2) {
      return 18;
    } else if (menuType == MenuType.GENERIC_9x3) {
      return 27;
    } else if (menuType == MenuType.GENERIC_9x4) {
      return 36;
    } else if (menuType == MenuType.GENERIC_9x5) {
      return 45;
    } else if (menuType == MenuType.GENERIC_9x6) {
      return 54;
    }
    // This is a bit gross, but allows us a safe fallthrough.
    return menuType.create(-1, viewer.getInventory()).slots.size() - BOTTOM_INVENTORY_SIZE;
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
  @Override
  protected boolean moveItemStackTo(ItemStack itemStack, int rangeLow, int rangeHigh, boolean topDown) {
    boolean modified = false;
    boolean stackable = itemStack.isStackable();
    Slot firstEmpty = null;

    for (int index = topDown ? rangeHigh - 1 : rangeLow;
         !itemStack.isEmpty() && (topDown ? index >= rangeLow : index < rangeHigh);
         index += topDown ? -1 : 1
    ) {
      Slot slot = slots.get(index);
      // If the slot cannot be added to, check the next slot.
      if (slot.isFake() || !slot.mayPlace(itemStack)) {
        continue;
      }

      if (slot.hasItem()) {
        // If the item isn't stackable, check the next slot.
        if (!stackable) {
          continue;
        }
        // Otherwise, add as many as we can from our stack to the slot.
        modified |= addToExistingStack(itemStack, slot);
      } else {
        // If this is the first empty slot, keep track of it for later use.
        if (firstEmpty == null) {
          firstEmpty = slot;
        }
        // If the item isn't stackable, we've located the slot we're adding it to, so we're done.
        if (!stackable) {
          break;
        }
      }
    }

    // If the item hasn't been fully added yet, add as many as we can to the first open slot.
    if (!itemStack.isEmpty() && firstEmpty != null) {
      firstEmpty.setByPlayer(itemStack.split(Math.min(itemStack.getCount(), firstEmpty.getMaxStackSize(itemStack))));
      firstEmpty.setChanged();
      modified = true;
    }

    return modified;
  }

  private static boolean addToExistingStack(ItemStack itemStack, Slot slot) {
    ItemStack existing = slot.getItem();

    // If the items aren't the same, we can't add our item.
    if (!ItemStack.isSameItemSameComponents(itemStack, existing)) {
      return false;
    }

    int max = slot.getMaxStackSize(existing);
    int existingCount = existing.getCount();

    // If the stack is already full, we can't add more.
    if (existingCount >= max) {
      return false;
    }

    int total = existingCount + itemStack.getCount();

    // If the existing item can accept the entirety of our item, we're done!
    if (total <= max) {
      itemStack.setCount(0);
      existing.setCount(total);
      slot.setChanged();
      return true;
    }

    // Otherwise, add as many as we can.
    itemStack.shrink(max - existingCount);
    existing.setCount(max);
    slot.setChanged();
    return true;
  }

  @Override
  public void clicked(int i, int j, @NotNull ClickType clickType, @NotNull Player player) {
    if (viewOnly) {
      if (clickType == ClickType.QUICK_CRAFT) {
        sendAllDataToRemote();
      }
      return;
    }
    super.clicked(i, j, clickType, player);
  }

  @Override
  public boolean stillValid(@NotNull Player player) {
    return true;
  }

}
