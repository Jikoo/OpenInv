package com.lishid.openinv.internal.paper26_3.container.slot;

import com.github.jikoo.openinv.internal.container.slot.Content;
import com.lishid.openinv.internal.paper26_3.container.slot.placeholder.Placeholders;
import com.lishid.openinv.internal.paper26_3.player.OpenPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * A slot in a survival crafting inventory. Unavailable when not online in a survival mode.
 */
@NullMarked
public class ContentCrafting implements Content<ServerPlayer, ItemStack, Container, Slot> {

  private final int index;
  protected ServerPlayer holder;
  protected List<ItemStack> items;

  protected ContentCrafting(ServerPlayer holder, int index) {
    setHolder(holder);
    this.index = index;
  }

  public static boolean isAvailable(ServerPlayer holder) {
    // Player must be online and not in creative - since the creative client is (semi-)authoritative,
    // it ignores changes without extra help, and will delete the item as a result.
    // Spectator mode is technically possible but may cause the item to be dropped if the client opens an inventory.
    return OpenPlayer.isConnected(holder.connection) && holder.gameMode.isSurvival();
  }

  @Override
  public void setHolder(ServerPlayer holder) {
    this.holder = holder;
    // Note: CraftingContainer#getItems is immutable! Be careful with updates.
    this.items = holder.inventoryMenu.getCraftSlots().getContents();
  }

  @Override
  public ItemStack get() {
    return isAvailable(holder) ? items.get(index) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack remove() {
    if (!isAvailable(holder)) {
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
    if (!isAvailable(holder)) {
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
    if (isAvailable(holder)) {
      items.set(index, itemStack);
      holder.inventoryMenu.slotsChanged(holder.inventoryMenu.getCraftSlots());
    } else {
      this.holder.drop(itemStack, false, Prediction.SERVER_ONLY);
    }
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotCrafting(container, slot, x, y);
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return isAvailable(holder) ? InventoryType.SlotType.CRAFTING : InventoryType.SlotType.OUTSIDE;
  }

  public class SlotCrafting extends SlotPlaceholder {

    private SlotCrafting(Container container, int index, int x, int y) {
      super(container, index, x, y);
    }

    @Override
    public ItemStack getOrDefault() {
      return isAvailable(holder) ? items.get(ContentCrafting.this.index) : Placeholders.survivalOnly(holder);
    }

    @Override
    public boolean mayPickup(Player player) {
      return isAvailable(holder);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
      return isAvailable(holder);
    }

    @Override
    public boolean hasItem() {
      return isAvailable(holder) && super.hasItem();
    }

    @Override
    public boolean isFake() {
      return !isAvailable(holder);
    }

  }

}
