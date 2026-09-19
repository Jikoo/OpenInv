package com.github.jikoo.openinv.internal.spigot26_3.container.slot;

import com.github.jikoo.openinv.internal.spigot26_3.player.OpenPlayer;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class ContentOffHand extends ContentEquipment {

  private ServerPlayer holder;

  public ContentOffHand(ServerPlayer holder) {
    super(holder, EquipmentSlot.OFFHAND);
    this.holder = holder; // Redundant, but silences nullity warning.
  }

  @Override
  public void setHolder(ServerPlayer holder) {
    super.setHolder(holder);
    this.holder = holder;
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    return InventoryType.SlotType.QUICKBAR;
  }

  @Override
  public Slot asSlot(Container container, int slot, int x, int y) {
    return new SlotEquipment(container, slot, x, y) {
      @Override
      public void setChanged() {
        if (OpenPlayer.isConnected(holder.connection) && !Objects.equals(holder.containerMenu, holder.inventoryMenu)) {
          holder.connection.send(
              new ClientboundContainerSetSlotPacket(
                  holder.inventoryMenu.containerId,
                  holder.inventoryMenu.incrementStateId(),
                  InventoryMenu.SHIELD_SLOT,
                  holder.getOffhandItem()
              ));
        }
      }
    };
  }

}
