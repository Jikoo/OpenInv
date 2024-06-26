package com.lishid.openinv.internal.v1_21_R1.inventory;

import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.InventoryViewTitle;
import com.lishid.openinv.internal.v1_21_R1.PlayerDataManager;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenInventory implements Container, Nameable, MenuProvider, ISpecialPlayerInventory {

  private final ContainerSlot dropSlot;
  private final List<ContainerSlot> slots = new ArrayList<>();
  private final int size;
  private ServerPlayer owner;
  private int maxStackSize = 99;
  private CraftInventory bukkitEntity;
  public List<HumanEntity> transaction = new ArrayList<>();

  public OpenInventory(@NotNull org.bukkit.entity.Player bukkitPlayer) {
    owner = PlayerDataManager.getHandle(bukkitPlayer);
    dropSlot = new DropContainerSlot(this.owner);

    // Get total size, rounding up to nearest 9 for client compatibility.
    int rawSize = owner.getInventory().getContainerSize() + owner.inventoryMenu.getCraftSlots().getContainerSize() + 1;
    size = ((int) Math.ceil(rawSize / 9.0)) * 9;

    slots.clear();

    for (int index = 0; index < size; ++index) {
      slots.add(createSlot(index));
    }
  }

  private @NotNull ContainerSlot createSlot(int index) {
    Inventory ownerInv = owner.getInventory();

    // Top of inventory: Regular contents
    int listSize = ownerInv.items.size();
    if (index < listSize) {
      InventoryType.SlotType type;
      // Hotbar slots are 0-8. We want those to appear on the bottom of the inventory like a normal player inventory,
      // so everything else needs to move up a row.
      int hotbarDiff = listSize - 9;
      if (index < hotbarDiff) {
        index += 9;
        type = InventoryType.SlotType.CONTAINER;
      } else {
        type = InventoryType.SlotType.QUICKBAR;
        index -= hotbarDiff;
      }
      return new ListContainerSlot(owner, index, type) {
        @Override
        public void setHolder(@NotNull ServerPlayer holder) {
          this.items = owner.getInventory().items;
        }
      };
    }
    index -= listSize;

    // Armor contents
    listSize = ownerInv.armor.size();
    if (index < listSize) {
      // We display armor "top down," so helmet should be leftmost item.
      index = switch (index) {
        case 3 -> 0;
        case 2 -> 1;
        case 1 -> 2;
        case 0 -> 3;
        // In the event that new armor slots are added, they can be placed at the end.
        default -> index;
      };
      return new ListContainerSlot(owner, index, InventoryType.SlotType.ARMOR) {
        @Override
        public void setHolder(@NotNull ServerPlayer holder) {
          this.items = owner.getInventory().armor;
        }
      };
    }
    index -= listSize;

    // Off-hand contents
    listSize = ownerInv.offhand.size();
    if (index < listSize) {
      return new ListContainerSlot(owner, index, InventoryType.SlotType.QUICKBAR) {
        @Override
        public void setHolder(@NotNull ServerPlayer holder) {
          this.items = holder.getInventory().offhand;
        }
      };
    }
    index -= listSize;

    // Crafting contents
    listSize = owner.inventoryMenu.getCraftSlots().getContents().size();
    if (index < listSize) {
      // TODO
      //  - no offline edits
      //  - extract to separate class
      //  - manipulate indices to make square
      return new ListContainerSlot(owner, index, InventoryType.SlotType.CRAFTING) {
        @Override
        public void setHolder(@NotNull ServerPlayer holder) {
          this.items = owner.inventoryMenu.getCraftSlots().getContents();
        }
      };
    }
    index -= listSize;

    // Cursor contents
    if (index == 0) {
      return new CursorContainerSlot(this.owner);
    }

    return dropSlot;
  }

  public Slot getMenuSlot(int index, int x, int y) {
    return slots.get(index).asMenuSlot(this, index, x, y);
  }

  public InventoryType.SlotType getSlotType(int index) {
    return slots.get(index).getSlotType();
  }

  public ServerPlayer getOwnerHandle() {
    return owner;
  }

  @Override
  public @NotNull org.bukkit.inventory.Inventory getBukkitInventory() {
    if (bukkitEntity == null) {
      bukkitEntity = new OpenPlayerInventory(this);
    }
    return bukkitEntity;
  }

  @Override
  public void setPlayerOnline(@NotNull org.bukkit.entity.Player player) {
    ServerPlayer newOwner = PlayerDataManager.getHandle(player);
    // Only transfer regular inventory - crafting and cursor slots are transient.
    newOwner.getInventory().replaceWith(owner.getInventory());
    // Update slots to point to new inventory.
    slots.forEach(slot -> slot.setHolder(newOwner));
    owner = newOwner;
  }

  @Override
  public void setPlayerOffline() {}

  @Override
  public @NotNull org.bukkit.entity.Player getPlayer() {
    return getOwner();
  }

  @Override
  public int getContainerSize() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return slots.stream().map(ContainerSlot::get).allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getItem(int index) {
    return slots.get(index).get();
  }

  @Override
  public ItemStack removeItem(int index, int amount) {
    return slots.get(index).removePartial(amount);
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    return slots.get(index).remove();
  }

  @Override
  public void setItem(int index, ItemStack itemStack) {
    slots.get(index).set(itemStack);
  }

  @Override
  public int getMaxStackSize() {
    return maxStackSize;
  }

  @Override
  public void setMaxStackSize(int maxStackSize) {
    this.maxStackSize = maxStackSize;
  }

  @Override
  public void setChanged() {}

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public List<ItemStack> getContents() {
    NonNullList<ItemStack> contents = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    for (int i = 0; i < getContainerSize(); ++i) {
      contents.set(i, getItem(i));
    }
    return contents;
  }

  @Override
  public void onOpen(CraftHumanEntity viewer) {
    transaction.add(viewer);
  }

  @Override
  public void onClose(CraftHumanEntity viewer) {
    transaction.remove(viewer);
  }

  @Override
  public List<HumanEntity> getViewers() {
    return transaction;
  }

  @Override
  public org.bukkit.entity.Player getOwner() {
    return owner.getBukkitEntity();
  }

  @Override
  public Location getLocation() {
    return owner.getBukkitEntity().getLocation();
  }

  @Override
  public void clearContent() {
    owner.getInventory().clearContent();
    owner.inventoryMenu.getCraftSlots().clearContent();
    owner.containerMenu.setCarried(ItemStack.EMPTY);
  }

  @Override
  public Component getName() {
    // This isn't quite accurate (uses language of opened player), but we override it in our InventoryView.
    return Component.literal(InventoryViewTitle.PLAYER_INVENTORY.getTitle(owner.getBukkitEntity(), this));
  }

  @Override
  public Component getDisplayName() {
    return getName();
  }

  @Override
  public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
    if (player instanceof ServerPlayer serverPlayer) {
      return new OpenInventoryMenu(this, serverPlayer, i);
    }
    return null;
  }

}
