package com.lishid.openinv.internal.paper1_21_5;

import com.lishid.openinv.internal.paper1_21_5.container.slot.placeholder.PlaceholderLoaderLegacyParse;
import com.lishid.openinv.internal.paper1_21_5.player.PlayerManager;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
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
  public void reload(@NotNull ConfigurationSection config) {
    ConfigurationSection placeholders = config.getConfigurationSection("placeholders");
    try {
      // Reset placeholders to defaults and try to load configuration.
      new PlaceholderLoaderLegacyParse().load(placeholders);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Caught exception loading placeholder overrides!", e);
    }
  }

}
