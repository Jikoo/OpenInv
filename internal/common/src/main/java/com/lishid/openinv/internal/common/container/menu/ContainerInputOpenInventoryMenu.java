package com.lishid.openinv.internal.common.container.menu;

import com.lishid.openinv.internal.common.container.BaseOpenInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import org.jetbrains.annotations.NotNull;

/**
 * 26.1+ dispatch variant of {@link OpenInventoryMenu}. Holds the
 * {@code clicked(ContainerInput, ...)} override so that 1.21.1-1.21.10 adapters
 * (which must extend {@link OpenInventoryMenu} directly) never see
 * {@code ContainerInput} in their superclass method tables.
 */
public class ContainerInputOpenInventoryMenu extends OpenInventoryMenu {

  public ContainerInputOpenInventoryMenu(
      BaseOpenInventory inventory,
      ServerPlayer viewer,
      int i,
      boolean viewOnly
  ) {
    super(inventory, viewer, i, viewOnly);
  }

  @Override
  public void clicked(int i, int j, @NotNull ContainerInput containerInput, @NotNull Player player) {
    if (isViewOnly()) {
      if (containerInput == ContainerInput.QUICK_CRAFT) {
        sendAllDataToRemote();
      }
      return;
    }
    super.clicked(i, j, containerInput, player);
  }

}
