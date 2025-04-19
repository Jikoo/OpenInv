package com.lishid.openinv.internal.paper1_21_4.container;

import com.lishid.openinv.internal.paper1_21_4.container.menu.OpenEnderChestMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenEnderChest extends com.lishid.openinv.internal.common.container.OpenEnderChest{

  public OpenEnderChest(@NotNull Player player) {
    super(player);
  }

  public @Nullable AbstractContainerMenu createMenu(net.minecraft.world.entity.player.Player player, int i, boolean viewOnly) {
    if (player instanceof ServerPlayer serverPlayer) {
      return new OpenEnderChestMenu(this, serverPlayer, i, viewOnly);
    }
    return null;
  }

}
