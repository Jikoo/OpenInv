package com.lishid.openinv.internal.paper1_21_4.container.menu;

import com.google.common.base.Suppliers;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.InternalOwned;
import com.lishid.openinv.internal.common.container.menu.OpenChestMenu;
import com.lishid.openinv.internal.common.container.slot.SlotPlaceholder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * An extension of {@link AbstractContainerMenu} that supports {@link SlotPlaceholder placeholders}.
 */
@SuppressWarnings("HidingField")
public abstract class OpenSyncMenu<T extends Container & ISpecialInventory & InternalOwned<ServerPlayer>>
    extends OpenChestMenu<T> {

  // Syncher fields
  private @Nullable ContainerSynchronizer synchronizer;
  private final List<DataSlot> dataSlots = new ArrayList<>();
  private final IntList remoteDataSlots = new IntArrayList();
  private final List<ContainerListener> containerListeners = new ArrayList<>();
  private ItemStack remoteCarried = ItemStack.EMPTY;
  private boolean suppressRemoteUpdates;

  protected OpenSyncMenu(
      @NotNull MenuType<ChestMenu> type,
      int containerCounter,
      @NotNull T container,
      @NotNull ServerPlayer viewer,
      boolean viewOnly
  ) {
    super(type, containerCounter, container, viewer, viewOnly);
  }

  // Overrides from here on are purely to modify the sync process to send placeholder items.
  @Override
  protected @NotNull Slot addSlot(@NotNull Slot slot) {
    slot.index = this.slots.size();
    this.slots.add(slot);
    this.lastSlots.add(ItemStack.EMPTY);
    this.remoteSlots.add(ItemStack.EMPTY);
    return slot;
  }

  @Override
  protected @NotNull DataSlot addDataSlot(@NotNull DataSlot dataSlot) {
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
  public void addSlotListener(@NotNull ContainerListener containerListener) {
    if (!this.containerListeners.contains(containerListener)) {
      this.containerListeners.add(containerListener);
      this.broadcastChanges();
    }
  }

  @Override
  public void setSynchronizer(@NotNull ContainerSynchronizer containerSynchronizer) {
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
  public void removeSlotListener(@NotNull ContainerListener containerListener) {
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
