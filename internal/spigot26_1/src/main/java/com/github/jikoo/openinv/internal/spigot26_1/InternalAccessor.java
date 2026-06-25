package com.github.jikoo.openinv.internal.spigot26_1;

import com.github.jikoo.openinv.internal.spigot26_1.player.PlayerManager;
import com.lishid.openinv.util.lang.LanguageManager;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public class InternalAccessor extends com.github.jikoo.openinv.internal.spigot26_2.InternalAccessor {

  private final PlayerManager manager;

  public InternalAccessor(Logger logger, LanguageManager lang) {
    super(logger, lang);
    this.manager = new PlayerManager(logger);
  }

  @Override
  public PlayerManager getPlayerManager() {
    return manager;
  }

}
