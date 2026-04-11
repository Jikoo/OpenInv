package com.lishid.openinv.internal.paper1_21_11.container.menu;

import com.lishid.openinv.internal.paper26_1.container.BaseOpenInventory;
import com.lishid.openinv.internal.paper26_1.container.menu.BaseOpenInventoryMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenInventoryMenu extends BaseOpenInventoryMenu {

  public OpenInventoryMenu(BaseOpenInventory inventory, ServerPlayer viewer, int i, boolean viewOnly) {
    super(inventory, viewer, i, viewOnly);
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
