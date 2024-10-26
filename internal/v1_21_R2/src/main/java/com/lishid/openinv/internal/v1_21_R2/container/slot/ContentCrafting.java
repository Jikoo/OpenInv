package com.lishid.openinv.internal.v1_21_R2.container.slot;

import com.lishid.openinv.internal.v1_21_R2.container.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A slot in a survival crafting inventory. Unavailable when not online in a survival mode.
 */
public class ContentCrafting implements Content {

  private final int index;
  private ServerPlayer holder;
  private List<ItemStack> items;

  public ContentCrafting(@NotNull ServerPlayer holder, int index) {
    setHolder(holder);
    this.index = index;
  }

  private boolean isAvailable() {
    return isAvailable(holder);
  }

  static boolean isAvailable(@NotNull ServerPlayer holder) {
    // Player must be online and not in creative - since the creative client is (semi-)authoritative,
    // it ignores changes without extra help, and will delete the item as a result.
    // Spectator mode is technically possible but may cause the item to be dropped if the client opens an inventory.
    return holder.connection != null && !holder.connection.isDisconnected() && holder.gameMode.isSurvival();
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.holder = holder;
    // Note: CraftingContainer#getItems is immutable! Be careful with updates.
    this.items = holder.inventoryMenu.getCraftSlots().getContents();
  }

  @Override
  public ItemStack get() {
    return isAvailable() ? items.get(index) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack remove() {
    if (!this.isAvailable()) {
      return ItemStack.EMPTY;
    }
    ItemStack removed = items.remove(index);
    if (removed.isEmpty()) {
      return ItemStack.EMPTY;
    }
    holder.inventoryMenu.slotsChanged(holder.inventoryMenu.getCraftSlots());
    return removed;
  }

  @Override
  public ItemStack removePartial(int amount) {
    if (!this.isAvailable()) {
      return ItemStack.EMPTY;
    }
    ItemStack removed = ContainerHelper.removeItem(items, index, amount);
    if (removed.isEmpty()) {
      return ItemStack.EMPTY;
    }
    holder.inventoryMenu.slotsChanged(holder.inventoryMenu.getCraftSlots());
    return removed;
  }

  @Override
  public void set(ItemStack itemStack) {
    if (isAvailable()) {
      items.set(index, itemStack);
      holder.inventoryMenu.slotsChanged(holder.inventoryMenu.getCraftSlots());
    } else {
      holder.drop(itemStack, false);
    }
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotCrafting(container, slot, x, y);
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return isAvailable() ? InventoryType.SlotType.CRAFTING : InventoryType.SlotType.OUTSIDE;
  }

  public class SlotCrafting extends SlotPlaceholder {

    private SlotCrafting(Container container, int index, int x, int y) {
      super(container, index, x, y);
    }

    @Override
    public ItemStack getOrDefault() {
      return isAvailable() ? items.get(ContentCrafting.this.index) : Placeholders.survivalOnly(holder);
    }

    @Override
    public boolean mayPickup(Player player) {
      return isAvailable();
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
      return isAvailable();
    }

    @Override
    public boolean hasItem() {
      return isAvailable() && super.hasItem();
    }

    @Override
    public boolean isFake() {
      return !isAvailable();
    }

  }

}
