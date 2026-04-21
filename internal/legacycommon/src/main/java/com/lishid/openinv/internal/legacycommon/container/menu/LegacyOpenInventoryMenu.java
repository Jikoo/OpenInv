package com.lishid.openinv.internal.legacycommon.container.menu;

import com.lishid.openinv.internal.common.container.BaseOpenInventory;
import com.lishid.openinv.internal.common.container.menu.OpenInventoryMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

/**
 * 1.21.1-1.21.10 variant of {@link OpenInventoryMenu} that overrides the NMS
 * click handler using the pre-26.1 {@code ClickType} enum. Common's override
 * uses {@code ContainerInput} (the 26.1 rename), which is not a valid override
 * on older servers, so the view-only QUICK_CRAFT re-sync hook was silently
 * lost for legacy versions. Restoring it here.
 */
public class LegacyOpenInventoryMenu extends OpenInventoryMenu {

  public LegacyOpenInventoryMenu(BaseOpenInventory inventory, ServerPlayer viewer, int i, boolean viewOnly) {
    super(inventory, viewer, i, viewOnly);
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
