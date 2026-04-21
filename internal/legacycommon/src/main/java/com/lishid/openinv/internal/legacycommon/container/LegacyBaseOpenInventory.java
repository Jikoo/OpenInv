package com.lishid.openinv.internal.legacycommon.container;

import com.lishid.openinv.internal.common.container.BaseOpenInventory;
import com.lishid.openinv.internal.common.container.menu.OpenChestMenu;
import com.lishid.openinv.internal.legacycommon.container.menu.LegacyOpenInventoryMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract 1.21.1-1.21.10 base that builds menus whose NMS click handler uses
 * pre-26.1 {@code ClickType}. Legacy paper adapters extend this instead of
 * {@link BaseOpenInventory} directly.
 */
public abstract class LegacyBaseOpenInventory extends BaseOpenInventory {

  public LegacyBaseOpenInventory(@org.jetbrains.annotations.NotNull org.bukkit.entity.Player bukkitPlayer) {
    super(bukkitPlayer);
  }

  @Override
  public @Nullable OpenChestMenu<?> createMenu(Player player, int i, boolean viewOnly) {
    if (player instanceof ServerPlayer serverPlayer) {
      return new LegacyOpenInventoryMenu(this, serverPlayer, i, viewOnly);
    }
    return null;
  }

}
