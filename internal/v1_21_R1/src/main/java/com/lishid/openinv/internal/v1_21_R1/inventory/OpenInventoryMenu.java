package com.lishid.openinv.internal.v1_21_R1.inventory;

import com.google.common.base.Suppliers;
import com.lishid.openinv.internal.v1_21_R1.AnySilentContainer;
import com.lishid.openinv.util.Permissions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
          addSlot(new ContainerSlotUninteractable.SlotUninteractable(inventory, index, x, y));
          continue;
        }

        Slot slot = getUpperSlot(index, x, y, ownInv);

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

  private Slot getUpperSlot(int index, int x, int y, boolean ownInv) {
    Slot slot = inventory.getMenuSlot(index, x, y);

    // If the slot is cannot be interacted with there's nothing to configure.
    if (slot.getClass().equals(ContainerSlotUninteractable.SlotUninteractable.class)) {
      return slot;
    }

    // Remove drop slot if viewer is not allowed to use it.
    if (slot instanceof ContainerSlotDrop.SlotDrop
        && !Permissions.INVENTORY_SLOT_DROP.hasPermission(viewer.getBukkitEntity())) {
      return new ContainerSlotUninteractable.SlotUninteractable(inventory, index, x, y);
    }

    if (slot instanceof ContainerSlotEquipment.SlotEquipment equipment) {
      Permissions perm = switch (equipment.getEquipmentSlot()) {
        case HEAD -> Permissions.INVENTORY_SLOT_HEAD_ANY;
        case CHEST -> Permissions.INVENTORY_SLOT_CHEST_ANY;
        case LEGS -> Permissions.INVENTORY_SLOT_LEGS_ANY;
        case FEET -> Permissions.INVENTORY_SLOT_FEET_ANY;
        // Off-hand can hold anything, not just equipment.
        default -> null;
      };
      // If the viewer doesn't have permission, only allow equipment the viewee can equip in the slot.
      if (perm != null && !perm.hasPermission(viewer.getBukkitEntity())) {
        equipment.onlyEquipmentFor(inventory.getOwnerHandle());
      }
      // Equipment slots are a core part of the inventory, so they will always be shown.
      return slot;
    }

    // When viewing own inventory, only allow access to equipment and drop slots (equipment allowed above).
    if (ownInv && !(slot instanceof ContainerSlotDrop.SlotDrop)) {
      return new ContainerSlotUninteractable.SlotUninteractable(inventory, index, x, y);
    }

    return slot;
  }

  static MenuType<?> getMenuType(OpenInventory inventory, ServerPlayer viewer) {
    int size = inventory.getContainerSize();
    if (inventory.getOwnerHandle().equals(viewer)) {
      size -= viewer.getInventory().items.size();
      size = ((int) Math.ceil(size / 9.0)) * 9;
    }

    return AnySilentContainer.getContainers(size);
  }

  @Override
  public CraftInventoryView getBukkitView() {
    if (bukkitEntity == null) {
      bukkitEntity = new CraftInventoryView(viewer.getBukkitEntity(), inventory.getBukkitInventory(), this) {
        @Override
        public org.bukkit.inventory.ItemStack getItem(int index) {
          if (index < 0) {
            return null;
          }

          Slot slot = slots.get(index);
          return CraftItemStack.asCraftMirror(slot.hasItem() ? slot.getItem() : ItemStack.EMPTY);
        }

        @Override
        public boolean isInTop(int rawSlot) {
          return rawSlot < topSize;
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

  //<editor-fold desc="Syncher overrides" defaultstate="collapsed">
  // Back at it again, overriding a bunch of methods because we need access to 2 fields.
  private @Nullable ContainerSynchronizer synchronizer;
  private final List<DataSlot> dataSlots = new ArrayList<>();
  private final IntList remoteDataSlots = new IntArrayList();
  private final List<ContainerListener> containerListeners = new ArrayList<>();
  private ItemStack remoteCarried = ItemStack.EMPTY;
  private boolean suppressRemoteUpdates;

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
      this.remoteSlots.set(index, (slot instanceof MenuSlotPlaceholder placeholder ? placeholder.getOrDefault() : slot.getItem()).copy());
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
      ItemStack itemstack = slot instanceof MenuSlotPlaceholder placeholder ? placeholder.getOrDefault() : slot.getItem();
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
  //</editor-fold>

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    // See ChestMenu and InventoryMenu
    Slot slot = this.slots.get(index);

    if (!slot.hasItem() || slot.isFake()) {
      return ItemStack.EMPTY;
    }

    ItemStack itemStack = slot.getItem();
    ItemStack originalStack = itemStack.copy();

    if (index < topSize) {
      // If we're moving top to bottom, do a normal transfer.
      if (!this.moveItemStackTo(itemStack, topSize, this.slots.size(), true)) {
        return ItemStack.EMPTY;
      }
    } else {
      EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(itemStack);
      boolean movedGear = switch (equipmentSlot) {
        // If this is gear, try to move it to the correct slot first.
        case OFFHAND, FEET, LEGS, CHEST, HEAD -> {
          // Locate the correct slot in the contents following the main inventory.
          for (int extra = inventory.getOwnerHandle().getInventory().items.size() - offset; extra < topSize; ++extra) {
            Slot extraSlot = getSlot(extra);
            if (extraSlot instanceof ContainerSlotEquipment.SlotEquipment equipSlot
                && equipSlot.getEquipmentSlot() == equipmentSlot) {
              // If we've found a matching slot, try to move to it.
              // If this succeeds, even partially, we will not attempt to move to other slots.
              // Otherwise, armor is already occupied, so we'll fall through to main inventory.
              yield this.moveItemStackTo(itemStack, extra, extra + 1, false);
            }
          }
          yield false;
        }
        // Non-gear gets no special treatment.
        default -> false;
      };

      // If main inventory is not available, there's nowhere else to move.
      if (offset != 0) {
        if (!movedGear) {
          return ItemStack.EMPTY;
        }
      } else {
        // If we didn't move to a gear slot, try to move to a main inventory slot.
        if (!movedGear && !this.moveItemStackTo(itemStack, 0, inventory.getOwnerHandle().getInventory().items.size(), true)) {
          return ItemStack.EMPTY;
        }
      }
    }

    if (itemStack.isEmpty()) {
      slot.setByPlayer(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }

    return originalStack;
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
        modified = addToExistingStack(itemStack, slot);
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
    return !(slot instanceof ContainerSlotDrop.SlotDrop || slot instanceof ContainerSlotUninteractable.SlotUninteractable);
  }

}
