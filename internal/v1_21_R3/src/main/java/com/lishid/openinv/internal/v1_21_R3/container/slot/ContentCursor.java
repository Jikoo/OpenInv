package com.lishid.openinv.internal.v1_21_R3.container.slot;

import com.lishid.openinv.internal.v1_21_R3.container.Placeholders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * A slot wrapping the active menu's cursor. Unavailable when not online in a survival mode.
 */
public class ContentCursor implements Content {

  private @NotNull ServerPlayer holder;

  public ContentCursor(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public ItemStack get() {
    return isAvailable() ? holder.containerMenu.getCarried() : ItemStack.EMPTY;
  }

  @Override
  public ItemStack remove() {
    ItemStack carried = holder.containerMenu.getCarried();
    holder.containerMenu.setCarried(ItemStack.EMPTY);
    return carried;
  }

  @Override
  public ItemStack removePartial(int amount) {
    ItemStack carried = holder.containerMenu.getCarried();
    if (!carried.isEmpty() && carried.getCount() >= amount) {
      ItemStack value = carried.split(amount);
      if (carried.isEmpty()) {
        holder.containerMenu.setCarried(ItemStack.EMPTY);
      }
      return value;
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void set(ItemStack itemStack) {
    if (isAvailable()) {
      holder.containerMenu.setCarried(itemStack);
    } else {
      holder.drop(itemStack, false);
    }
  }

  private boolean isAvailable() {
    // Player must be online and not in creative - since the creative client is (semi-)authoritative,
    // it ignores changes without extra help, and will delete the item as a result.
    // Spectator mode is technically possible but may cause the item to be dropped if the client opens an inventory.
    return holder.connection != null && !holder.connection.isDisconnected() && holder.gameMode.isSurvival();
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotCursor(container, slot, x, y);
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    // As close as possible to "not real"
    return InventoryType.SlotType.OUTSIDE;
  }

  public class SlotCursor extends SlotPlaceholder {

    private SlotCursor(Container container, int index, int x, int y) {
      super(container, index, x, y);
    }

    @Override
    public ItemStack getOrDefault() {
      if (!isAvailable()) {
        return Placeholders.survivalOnly(holder);
      }
      ItemStack carried = holder.containerMenu.getCarried();
      return carried.isEmpty() ? Placeholders.cursor : carried;
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
      return true;
    }

  }

}
