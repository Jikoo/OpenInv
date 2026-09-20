package com.lishid.openinv.internal.paper1_21_11.container.menu;

import com.github.jikoo.openinv.internal.container.slot.InventoryFactory;
import com.lishid.openinv.internal.paper26_3.container.BaseOpenInventory;
import com.lishid.openinv.internal.paper26_3.container.menu.BaseOpenInventoryMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenInventoryMenu extends BaseOpenInventoryMenu {

  public OpenInventoryMenu(
      InventoryFactory<ServerPlayer, ItemStack, Container, Slot, EquipmentSlot> factory,
      BaseOpenInventory inventory,
      ServerPlayer viewer,
      int i,
      boolean viewOnly
  ) {
    super(factory, inventory, viewer, i, viewOnly);
  }

  @Override
  public void clicked(int i, int j, ClickType clickType, Player player) {
    if (viewOnly) {
      if (clickType == ClickType.QUICK_CRAFT) {
        sendAllDataToRemote();
      }
      return;
    }
    super.clicked(i, j, clickType, player);
  }

}
