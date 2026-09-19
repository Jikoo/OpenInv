package com.lishid.openinv.internal.paper26_1;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.paper26_1.container.AnySilentContainer;
import com.lishid.openinv.internal.paper26_1.container.OpenEnderChest;
import com.lishid.openinv.internal.paper26_1.player.PlayerManager;
import com.lishid.openinv.internal.paper26_3.container.slot.placeholder.PlaceholderLoader;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public class InternalAccessor extends com.lishid.openinv.internal.paper26_3.InternalAccessor {

  public InternalAccessor(Logger logger, LanguageManager lang) {
    super(logger, lang);
  }

  @Override
  protected PlayerManager createPlayerManager(Logger logger) {
    return new PlayerManager(logger);
  }

  @Override
  protected AnySilentContainer createAnySilentContainer(
      Logger logger,
      LanguageManager lang
  ) {
    return new AnySilentContainer(logger, lang);
  }

  @Override
  protected PlaceholderLoader createPlaceholderLoader() {
    return new com.lishid.openinv.internal.paper26_1.container.slot.placeholder.PlaceholderLoader();
  }

  @Override
  public ISpecialEnderChest createEnderChest(Player player) {
    return new OpenEnderChest(player);
  }

}
