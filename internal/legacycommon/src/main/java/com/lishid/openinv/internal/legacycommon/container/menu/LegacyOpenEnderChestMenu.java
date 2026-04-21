package com.lishid.openinv.internal.legacycommon.container.menu;

import com.lishid.openinv.internal.common.container.OpenEnderChest;
import com.lishid.openinv.internal.common.container.menu.OpenEnderChestMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

public class LegacyOpenEnderChestMenu extends OpenEnderChestMenu {

  public LegacyOpenEnderChestMenu(
      @NotNull OpenEnderChest enderChest,
      @NotNull ServerPlayer viewer,
      int containerId,
      boolean viewOnly
  ) {
    super(enderChest, viewer, containerId, viewOnly);
  }

  @Override
  public void clicked(int i, int j, @NotNull ClickType clickType, @NotNull Player player) {
    if (isViewOnly()) {
      if (clickType == ClickType.QUICK_CRAFT) {
        sendAllDataToRemote();
      }
      return;
    }
    super.clicked(i, j, clickType, player);
  }

}
