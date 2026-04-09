package com.lishid.openinv.internal.paper26_1.container.menu;

import com.google.common.base.Suppliers;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.InternalOwned;
import com.lishid.openinv.internal.paper26_1.container.slot.SlotPlaceholder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * An extension of {@link OpenChestMenu} that supports {@link SlotPlaceholder placeholders}.
 */
@NullMarked
public abstract class OpenSyncMenu<T extends Container & ISpecialInventory & InternalOwned<ServerPlayer>>
    extends OpenChestMenu<T> {

  // Syncher fields required to send placeholder items.
  // All fields intentionally shadow AbstractContainerMenu fields so that warnings
  // will be generated if they are no longer necessary.
  protected @Nullable ContainerSynchronizer synchronizer;
  protected final List<ContainerListener> listeners = new ArrayList<>();
  private @Nullable RemoteSlot remoteCarried = RemoteSlot.PLACEHOLDER;
  protected boolean suppressRemoteUpdates;

  protected OpenSyncMenu(
      MenuType<ChestMenu> type,
      int containerCounter,
      T container,
      ServerPlayer viewer,
      boolean viewOnly
  ) {
    super(type, containerCounter, container, viewer, viewOnly);
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

    // See private synchronizeCarriedToRemote
    if (!suppressRemoteUpdates && !getRemoteCarried().matches(getCarried())) {
      this.broadcastCarriedItem();
    }

    for (int index = 0; index < this.dataSlots.size(); ++index) {
      DataSlot dataSlot = this.dataSlots.get(index);
      int current = dataSlot.get();
      if (dataSlot.checkAndClearUpdateFlag()) {
        // See private updateDataSlotListeners
        for (ContainerListener containerListener : this.listeners) {
          containerListener.dataChanged(this, index, current);
        }
      }

      // See private synchronizeDataSlotToRemote
      if (!suppressRemoteUpdates && remoteDataSlots.getInt(index) != current) {
        remoteDataSlots.set(index, current);
        if (synchronizer != null) {
          synchronizer.sendDataChange(this, index, current);
        }
      }
    }
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

    getRemoteCarried().force(getCarried());

    for (int index = 0; index < this.dataSlots.size(); ++index) {
      this.remoteDataSlots.set(index, this.dataSlots.get(index).get());
    }

    if (this.synchronizer != null) {
      this.synchronizer.sendInitialData(this, contentsCopy, this.getCarried().copy(), this.remoteDataSlots.toIntArray());
      this.synchronizer.sendOffHandSlotChange();
    }
  }

  protected RemoteSlot getRemoteCarried() {
    if (remoteCarried != null) {
      return remoteCarried;
    }

    try {
      Field remoteCarried = AbstractContainerMenu.class.getDeclaredField("remoteCarried");
      remoteCarried.setAccessible(true);
      Object slot = remoteCarried.get(this);
      if (slot instanceof RemoteSlot remoteSlot) {
        this.remoteCarried = remoteSlot;
      } else {
        this.remoteCarried = RemoteSlot.PLACEHOLDER;
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      remoteCarried = RemoteSlot.PLACEHOLDER;
    }

    return remoteCarried;
  }

  // Overrides from here on are purely to capture the contents of fields that aren't exposed.
  // Note that super calls should likely always be last! Some send data to remotes after updating state.
  @Override
  public void addSlotListener(ContainerListener containerListener) {
    if (!this.listeners.contains(containerListener)) {
      this.listeners.add(containerListener);
    }
    super.addSlotListener(containerListener);
  }

  @Override
  public void setSynchronizer(ContainerSynchronizer containerSynchronizer) {
    this.synchronizer = containerSynchronizer;
    // Unset carried slot so we re-capture it when sending slots.
    // setSynchronizer calls sendAllDataToRemote, so we can't just do the reflection here once after the fact.
    this.remoteCarried = null;
    super.setSynchronizer(containerSynchronizer);
  }

  @Override
  public void removeSlotListener(ContainerListener containerListener) {
    this.listeners.remove(containerListener);
    super.removeSlotListener(containerListener);
  }

  @Override
  public void suppressRemoteUpdates() {
    this.suppressRemoteUpdates = true;
    super.suppressRemoteUpdates();
  }

  @Override
  public void resumeRemoteUpdates() {
    this.suppressRemoteUpdates = false;
    super.resumeRemoteUpdates();
  }

}
