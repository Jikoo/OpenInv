package com.github.jikoo.openinv.internal.spigot26_1;

import com.github.jikoo.openinv.internal.spigot26_1.container.slot.placeholder.PlaceholderLoader;
import com.github.jikoo.openinv.internal.spigot26_1.player.PlayerManager;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Level;
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

  @Override
  public void reload(ConfigurationSection config) {
    ConfigurationSection placeholders = config.getConfigurationSection("placeholders");
    try {
      // Reset placeholders to defaults and try to load configuration.
      new PlaceholderLoader().load(placeholders);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Caught exception loading placeholder overrides!", e);
    }
  }

}
