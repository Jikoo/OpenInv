package com.lishid.openinv.internal.paper1_21_11.container;

import com.lishid.openinv.internal.paper1_21_11.container.menu.OpenEnderChestMenu;
import com.lishid.openinv.internal.paper26_1.container.menu.OpenChestMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenEnderChest extends com.lishid.openinv.internal.paper26_1.container.OpenEnderChest {

  public OpenEnderChest(org.bukkit.entity.Player player) {
    super(player);
  }

  @Override
  public @Nullable OpenChestMenu<?> createMenu(Player player, int i, boolean viewOnly) {
    if (player instanceof ServerPlayer serverPlayer) {
      return new OpenEnderChestMenu(this, serverPlayer, i, viewOnly);
    }
    return null;
  }

}
