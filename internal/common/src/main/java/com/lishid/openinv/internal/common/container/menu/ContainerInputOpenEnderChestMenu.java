package com.lishid.openinv.internal.common.container.menu;

import com.lishid.openinv.internal.common.container.OpenEnderChest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import org.jetbrains.annotations.NotNull;

/**
 * 26.1+ dispatch variant of {@link OpenEnderChestMenu}. Holds the
 * {@code clicked(ContainerInput, ...)} override so that 1.21.1-1.21.10 adapters
 * (which must extend {@link OpenEnderChestMenu} directly) never see
 * {@code ContainerInput} in their superclass method tables.
 */
public class ContainerInputOpenEnderChestMenu extends OpenEnderChestMenu {

  public ContainerInputOpenEnderChestMenu(
      @NotNull OpenEnderChest enderChest,
      @NotNull ServerPlayer viewer,
      int containerId,
      boolean viewOnly
  ) {
    super(enderChest, viewer, containerId, viewOnly);
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
