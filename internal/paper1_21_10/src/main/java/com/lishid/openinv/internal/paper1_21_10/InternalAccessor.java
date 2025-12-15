package com.lishid.openinv.internal.paper1_21_10;

import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.paper1_21_10.container.OpenInventory;
import com.lishid.openinv.internal.paper1_21_10.player.PlayerManager;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class InternalAccessor extends com.lishid.openinv.internal.common.InternalAccessor {

  private final @NotNull PlayerManager manager;

  public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
    super(logger, lang);
    manager = new PlayerManager(logger);
  }

  @Override
  public @NotNull PlayerManager getPlayerManager() {
    return manager;
  }

  @Override
  public @NotNull ISpecialPlayerInventory createPlayerInventory(@NotNull Player player) {
    return new OpenInventory(player);
  }

}
