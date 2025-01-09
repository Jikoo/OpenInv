package com.lishid.openinv.internal.v1_21_R3.container.menu;

import com.google.common.base.Suppliers;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.InternalOwned;
import com.lishid.openinv.internal.v1_21_R3.container.bukkit.OpenDummyInventory;
import com.lishid.openinv.internal.v1_21_R3.container.slot.SlotPlaceholder;
import com.lishid.openinv.internal.v1_21_R3.container.slot.SlotViewOnly;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventoryView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * An extension of {@link AbstractContainerMenu} that supports {@link SlotPlaceholder placeholders}.
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
  // Syncher fields
  private @Nullable ContainerSynchronizer synchronizer;
  private final List<DataSlot> dataSlots = new ArrayList<>();
  private final IntList remoteDataSlots = new IntArrayList();
  private final List<ContainerListener> containerListeners = new ArrayList<>();
  private ItemStack remoteCarried = ItemStack.EMPTY;
  private boolean suppressRemoteUpdates;

  protected OpenChestMenu(
      @NotNull MenuType<ChestMenu> type,
      int containerCounter,
      @NotNull T container,
      @NotNull ServerPlayer viewer,
      boolean viewOnly) {
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

  protected void preSlotSetup() {}

  protected @NotNull Slot getUpperSlot(int index, int x, int y) {
    if (viewOnly) {
      return new SlotViewOnly(container, index, x, y);
    }
    return new Slot(container, index, x, y);
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
    if (menuType == null) {
      throw new IllegalStateException("MenuType cannot be null!");
    } else if (menuType == MenuType.GENERIC_9x1) {
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
  public void clicked(int i, int j, ClickType clickType, Player player) {
    if (viewOnly) {
      if (clickType == ClickType.QUICK_CRAFT) {
        sendAllDataToRemote();
      }
      return;
    }
    super.clicked(i, j, clickType, player);
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  // Overrides from here on are purely to modify the sync process to send placeholder items.
  @Override
  protected Slot addSlot(Slot slot) {
    slot.index = this.slots.size();
    this.slots.add(slot);
    this.lastSlots.add(ItemStack.EMPTY);
    this.remoteSlots.add(ItemStack.EMPTY);
    return slot;
  }

  @Override
  protected DataSlot addDataSlot(DataSlot dataSlot) {
    this.dataSlots.add(dataSlot);
    this.remoteDataSlots.add(0);
    return dataSlot;
  }

  @Override
  protected void addDataSlots(ContainerData containerData) {
    for (int i = 0; i < containerData.getCount(); i++) {
      this.addDataSlot(DataSlot.forContainer(containerData, i));
    }
  }

  @Override
  public void addSlotListener(ContainerListener containerListener) {
    if (!this.containerListeners.contains(containerListener)) {
      this.containerListeners.add(containerListener);
      this.broadcastChanges();
    }
  }

  @Override
  public void setSynchronizer(ContainerSynchronizer containerSynchronizer) {
    this.synchronizer = containerSynchronizer;
    this.sendAllDataToRemote();
  }

  @Override
  public void sendAllDataToRemote() {
    for (int index = 0; index < slots.size(); ++index) {
      Slot slot = slots.get(index);
      this.remoteSlots.set(index, (slot instanceof SlotPlaceholder placeholder ? placeholder.getOrDefault() : slot.getItem()).copy());
    }

    remoteCarried = getCarried().copy();

    for (int index = 0; index < this.dataSlots.size(); ++index) {
      this.remoteDataSlots.set(index, this.dataSlots.get(index).get());
    }

    if (this.synchronizer != null) {
      this.synchronizer.sendInitialData(this, this.remoteSlots, this.remoteCarried, this.remoteDataSlots.toIntArray());
    }
  }

  @Override
  public void broadcastCarriedItem() {
    this.remoteCarried = this.getCarried().copy();
    if (this.synchronizer != null) {
      this.synchronizer.sendCarriedChange(this, this.remoteCarried);
    }
  }

  @Override
  public void removeSlotListener(ContainerListener containerListener) {
    this.containerListeners.remove(containerListener);
  }

  @Override
  public void broadcastChanges() {
    for (int index = 0; index < this.slots.size(); ++index) {
      Slot slot = this.slots.get(index);
      ItemStack itemstack = slot instanceof SlotPlaceholder placeholder ? placeholder.getOrDefault() : slot.getItem();
      Supplier<ItemStack> supplier = Suppliers.memoize(itemstack::copy);
      this.triggerSlotListeners(index, itemstack, supplier);
      this.synchronizeSlotToRemote(index, itemstack, supplier);
    }

    this.synchronizeCarriedToRemote();

    for (int index = 0; index < this.dataSlots.size(); ++index) {
      DataSlot dataSlot = this.dataSlots.get(index);
      int j = dataSlot.get();
      if (dataSlot.checkAndClearUpdateFlag()) {
        this.updateDataSlotListeners(index, j);
      }

      this.synchronizeDataSlotToRemote(index, j);
    }
  }

  @Override
  public void broadcastFullState() {
    for (int index = 0; index < this.slots.size(); ++index) {
      ItemStack itemstack = this.slots.get(index).getItem();
      this.triggerSlotListeners(index, itemstack, itemstack::copy);
    }

    for (int index = 0; index < this.dataSlots.size(); ++index) {
      DataSlot containerproperty = this.dataSlots.get(index);
      if (containerproperty.checkAndClearUpdateFlag()) {
        this.updateDataSlotListeners(index, containerproperty.get());
      }
    }

    this.sendAllDataToRemote();
  }

  private void updateDataSlotListeners(int i, int j) {
    for (ContainerListener containerListener : this.containerListeners) {
      containerListener.dataChanged(this, i, j);
    }
  }

  private void triggerSlotListeners(int index, ItemStack itemStack, Supplier<ItemStack> supplier) {
    ItemStack itemStack1 = this.lastSlots.get(index);
    if (!ItemStack.matches(itemStack1, itemStack)) {
      ItemStack itemStack2 = supplier.get();
      this.lastSlots.set(index, itemStack2);

      for (ContainerListener containerListener : this.containerListeners) {
        containerListener.slotChanged(this, index, itemStack2);
      }
    }
  }

  private void synchronizeSlotToRemote(int i, ItemStack itemStack, Supplier<ItemStack> supplier) {
    if (!this.suppressRemoteUpdates) {
      ItemStack itemStack1 = this.remoteSlots.get(i);
      if (!ItemStack.matches(itemStack1, itemStack)) {
        ItemStack itemstack2 = supplier.get();
        this.remoteSlots.set(i, itemstack2);
        if (this.synchronizer != null) {
          this.synchronizer.sendSlotChange(this, i, itemstack2);
        }
      }
    }
  }

  private void synchronizeDataSlotToRemote(int index, int value) {
    if (!this.suppressRemoteUpdates) {
      int existing = this.remoteDataSlots.getInt(index);
      if (existing != value) {
        this.remoteDataSlots.set(index, value);
        if (this.synchronizer != null) {
          this.synchronizer.sendDataChange(this, index, value);
        }
      }
    }
  }

  private void synchronizeCarriedToRemote() {
    if (!this.suppressRemoteUpdates && !ItemStack.matches(this.getCarried(), this.remoteCarried)) {
      this.remoteCarried = this.getCarried().copy();
      if (this.synchronizer != null) {
        this.synchronizer.sendCarriedChange(this, this.remoteCarried);
      }
    }
  }

  @Override
  public void setRemoteCarried(ItemStack itemstack) {
    this.remoteCarried = itemstack.copy();
  }

  @Override
  public void suppressRemoteUpdates() {
    this.suppressRemoteUpdates = true;
  }

  @Override
  public void resumeRemoteUpdates() {
    this.suppressRemoteUpdates = false;
  }

}
