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

  private final List<ContainerSlot> slots;
  private final int size;
  private ServerPlayer owner;
  private int maxStackSize = 99;
  private CraftInventory bukkitEntity;
  public List<HumanEntity> transaction = new ArrayList<>();

  public OpenInventory(@NotNull org.bukkit.entity.Player bukkitPlayer) {
    owner = PlayerDataManager.getHandle(bukkitPlayer);

    // Get total size, rounding up to nearest 9 for client compatibility.
    int rawSize = owner.getInventory().getContainerSize() + owner.inventoryMenu.getCraftSlots().getContainerSize() + 1;
    size = ((int) Math.ceil(rawSize / 9.0)) * 9;

    slots = NonNullList.withSize(size, ContainerSlotEmpty.INSTANCE);
    setupSlots();
  }

  private void setupSlots() {
    // Top of inventory: Regular contents.
    int nextIndex = addMainInventory();

    int rawSize = owner.getInventory().getContainerSize() + owner.inventoryMenu.getCraftSlots().getContainerSize() + 1;

    // If inventory is expected size, we can arrange slots to be pretty.
    if (rawSize == 46) {
      // Armor slots: bottom left.
      addArmor(45);
      // Off-hand above chestplate.
      addOffHand(37);
      // Drop in the middle.
      slots.set(40, new ContainerSlotDrop(owner));
      // Cursor right below drop.
      slots.set(49, new ContainerSlotCursor(owner));
      // Crafting is displayed in a 2x2 in the bottom right corner.
      addCrafting(43, true);
      return;
    }

    // Otherwise we'll just add elements linearly.
    nextIndex = addArmor(nextIndex);
    nextIndex = addOffHand(nextIndex);
    nextIndex = addCrafting(nextIndex, false);
    slots.set(nextIndex, new ContainerSlotCursor(owner));
    // Drop slot last.
    slots.set(slots.size() - 1, new ContainerSlotDrop(owner));
  }

  private int addMainInventory() {
    int listSize = owner.getInventory().items.size();
    // Hotbar slots are 0-8. We want those to appear on the bottom of the inventory like a normal player inventory,
    // so everything else needs to move up a row.
    int hotbarDiff = listSize - 9;
    for (int localIndex = 0; localIndex < listSize; ++localIndex) {
      InventoryType.SlotType type;
      int invIndex;
      if (localIndex < hotbarDiff) {
        invIndex = localIndex + 9;
        type = InventoryType.SlotType.CONTAINER;
      } else {
        type = InventoryType.SlotType.QUICKBAR;
        invIndex = localIndex - hotbarDiff;
      }

      slots.set(localIndex, new ContainerSlotList(owner, invIndex, type) {
        @Override
        public void setHolder(@NotNull ServerPlayer holder) {
          this.items = owner.getInventory().items;
        }
      });
    }
    return listSize;
  }

  private int addArmor(int startIndex) {
    int listSize = owner.getInventory().armor.size();

    for (int i = 0; i < listSize; ++i) {
      // Armor slots go bottom to top; boots are slot 0, helmet is slot 3.
      // Since we have to display horizontally due to space restrictions,
      // making the left side the "top" is more user-friendly.
      int armorIndex = switch (i) {
        case 3 -> 0;
        case 2 -> 1;
        case 1 -> 2;
        case 0 -> 3;
        // In the event that new armor slots are added, they can be placed at the end.
        default -> i;
      };

      slots.set(startIndex + i, new ContainerSlotList(owner, armorIndex, InventoryType.SlotType.ARMOR) {
        @Override
        public void setHolder(@NotNull ServerPlayer holder) {
          this.items = owner.getInventory().armor;
        }
      });
    }

    return startIndex + listSize;
  }

  private int addOffHand(int startIndex) {
    int listSize = owner.getInventory().offhand.size();
    for (int localIndex = 0; localIndex < listSize; ++localIndex) {
      slots.set(startIndex + localIndex, new ContainerSlotList(owner, localIndex, InventoryType.SlotType.QUICKBAR) {
        @Override
        public void setHolder(@NotNull ServerPlayer holder) {
          this.items = holder.getInventory().offhand;
        }
      });
    }
    return startIndex + listSize;
  }

  private int addCrafting(int startIndex, boolean pretty) {
    int listSize = owner.inventoryMenu.getCraftSlots().getContents().size();
    for (int localIndex = 0; localIndex < listSize; ++localIndex) {
      // Pretty display is a 2x2 rather than linear.
      // If index is in top row, grid is not 2x2, or pretty is disabled, just use current index.
      // Otherwise, subtract 2 and add 9 to start in the same position on the next row.
      int modIndex = startIndex + (localIndex < 2 || listSize != 4 || !pretty ? localIndex : localIndex + 7);

      slots.set(modIndex, new ContainerSlotCrafting(owner, localIndex));
    }
    return startIndex + listSize;
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
