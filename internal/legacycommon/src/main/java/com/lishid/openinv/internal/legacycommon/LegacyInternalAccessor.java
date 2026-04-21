package com.lishid.openinv.internal.legacycommon;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.common.InternalAccessor;
import com.lishid.openinv.internal.legacycommon.container.LegacyOpenEnderChest;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * 1.21.1-1.21.10 InternalAccessor. Overrides {@link #createEnderChest} so
 * the ender-chest menu uses the pre-26.1 {@code ClickType} override. The
 * player-inventory menu factory lives on {@link
 * com.lishid.openinv.internal.legacycommon.container.LegacyBaseOpenInventory}
 * which each legacy paper adapter's {@code OpenInventory} extends directly.
 */
public class LegacyInternalAccessor extends InternalAccessor {

  public LegacyInternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
    super(logger, lang);
  }

  @Override
  public @NotNull ISpecialEnderChest createEnderChest(@NotNull Player player) {
    return new LegacyOpenEnderChest(player);
  }

}
