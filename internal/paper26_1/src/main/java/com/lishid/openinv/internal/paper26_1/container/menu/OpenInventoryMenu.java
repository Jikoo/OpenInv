package com.lishid.openinv.internal.paper26_1.container.menu;

import com.lishid.openinv.internal.paper26_1.container.BaseOpenInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenInventoryMenu extends BaseOpenInventoryMenu {

  public OpenInventoryMenu(BaseOpenInventory inventory, ServerPlayer viewer, int i, boolean viewOnly) {
    super(inventory, viewer, i, viewOnly);
  }

  @Override
  public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
    if (viewOnly) {
      if (containerInput == ContainerInput.QUICK_CRAFT) {
        sendAllDataToRemote();
      }
    }
    super.clicked(slotIndex, buttonNum, containerInput, player);
  }

}
