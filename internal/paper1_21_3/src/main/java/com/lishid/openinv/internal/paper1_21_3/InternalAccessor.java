package com.lishid.openinv.internal.paper1_21_3;

import com.lishid.openinv.internal.paper1_21_3.container.slot.placeholder.NumericDataPlaceholderLoader;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InternalAccessor extends com.lishid.openinv.internal.common.InternalAccessor {

  public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
    super(logger, lang);
  }

  @Override
  public void reload(@NotNull ConfigurationSection config) {
    ConfigurationSection placeholders = config.getConfigurationSection("placeholders");
    try {
      // Reset placeholders to defaults and try to load configuration.
      new NumericDataPlaceholderLoader().load(placeholders);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Caught exception loading placeholder overrides!", e);
    }
  }

}
