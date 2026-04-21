package com.lishid.openinv.internal.legacycommon.container;

import com.lishid.openinv.internal.common.container.OpenEnderChest;
import com.lishid.openinv.internal.common.container.menu.OpenChestMenu;
import com.lishid.openinv.internal.legacycommon.container.menu.LegacyOpenEnderChestMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21.1-1.21.10 variant of {@link OpenEnderChest} whose menu factory
 * emits a {@link LegacyOpenEnderChestMenu} (pre-26.1 ClickType override).
 */
public class LegacyOpenEnderChest extends OpenEnderChest {

  public LegacyOpenEnderChest(@NotNull org.bukkit.entity.Player player) {
    super(player);
  }

  @Override
  public @Nullable OpenChestMenu<?> createMenu(Player player, int i, boolean viewOnly) {
    if (player instanceof ServerPlayer serverPlayer) {
      return new LegacyOpenEnderChestMenu(this, serverPlayer, i, viewOnly);
    }
    return null;
  }

}
