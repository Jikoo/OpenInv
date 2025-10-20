package com.lishid.openinv.internal.common.container.menu;

import com.google.common.base.Suppliers;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.InternalOwned;
import com.lishid.openinv.internal.common.container.slot.SlotPlaceholder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.HashedStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * An extension of {@link OpenChestMenu} that supports {@link SlotPlaceholder placeholders}.
 */
@SuppressWarnings("HidingField") // Revisit when removing 1.21.4 support
public abstract class OpenSyncMenu<T extends Container & ISpecialInventory & InternalOwned<ServerPlayer>>
    extends OpenChestMenu<T> {

  // Syncher fields
  protected @Nullable ContainerSynchronizer synchronizer;
  protected final List<DataSlot> dataSlots = new ArrayList<>();
  protected final IntList remoteDataSlots = new IntArrayList();
  protected final List<ContainerListener> containerListeners = new ArrayList<>();
  private RemoteSlot remoteCarried = RemoteSlot.PLACEHOLDER;
  protected boolean suppressRemoteUpdates;

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
    this.remoteSlots.add(this.synchronizer != null ? this.synchronizer.createSlot() : RemoteSlot.PLACEHOLDER);
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
    this.remoteCarried = synchronizer.createSlot();
    this.remoteSlots.replaceAll(slot -> synchronizer.createSlot());
    this.sendAllDataToRemote();
  }

  @Override
  public void sendAllDataToRemote() {
    List<ItemStack> contentsCopy = new ArrayList<>();
    for (int index = 0; index < slots.size(); ++index) {
      Slot slot = slots.get(index);
      ItemStack itemStack = slot instanceof SlotPlaceholder placeholder ? placeholder.getOrDefault() : slot.getItem();
      contentsCopy.add(itemStack);
      this.remoteSlots.get(index).force(itemStack);
    }

    remoteCarried.force(getCarried());

    for (int index = 0; index < this.dataSlots.size(); ++index) {
      this.remoteDataSlots.set(index, this.dataSlots.get(index).get());
    }

    if (this.synchronizer != null) {
      this.synchronizer.sendInitialData(this, contentsCopy, this.getCarried().copy(), this.remoteDataSlots.toIntArray());
    }
  }

  @Override
  public void forceSlot(@NotNull Container container, int slot) {
    int slotsIndex = this.findSlot(container, slot).orElse(-1);
    if (slotsIndex != -1) {
      ItemStack item = this.slots.get(slotsIndex).getItem();
      this.remoteSlots.get(slotsIndex).force(item);
      if (this.synchronizer != null) {
        this.synchronizer.sendSlotChange(this, slotsIndex, item.copy());
      }
    }
  }

  @Override
  public void broadcastCarriedItem() {
    ItemStack carried = this.getCarried();
    this.remoteCarried.force(carried);
    if (this.synchronizer != null) {
      this.synchronizer.sendCarriedChange(this, carried.copy());
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

  @Override
  public void triggerSlotListeners(int index, @NotNull ItemStack itemStack, @NotNull Supplier<ItemStack> supplier) {
    ItemStack itemStack1 = this.lastSlots.get(index);
    if (!ItemStack.matches(itemStack1, itemStack)) {
      ItemStack itemStack2 = supplier.get();
      this.lastSlots.set(index, itemStack2);

      for (ContainerListener containerListener : this.containerListeners) {
        containerListener.slotChanged(this, index, itemStack2);
      }
    }
  }

  @Override
  public void synchronizeSlotToRemote(int i, @NotNull ItemStack itemStack, @NotNull Supplier<ItemStack> supplier) {
    if (!this.suppressRemoteUpdates) {
      RemoteSlot slot = this.remoteSlots.get(i);
      if (!slot.matches(itemStack)) {
        slot.force(itemStack);
        if (this.synchronizer != null) {
          this.synchronizer.sendSlotChange(this, i, supplier.get());
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
    if (!this.suppressRemoteUpdates) {
      ItemStack carried = this.getCarried();
      if (!this.remoteCarried.matches(carried)) {
        this.remoteCarried.force(carried);
        if (this.synchronizer != null) {
          this.synchronizer.sendCarriedChange(this, carried.copy());
        }
      }
    }
  }

  @Override
  public void setRemoteCarried(@NotNull HashedStack stack) {
    this.remoteCarried.receive(stack);
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
