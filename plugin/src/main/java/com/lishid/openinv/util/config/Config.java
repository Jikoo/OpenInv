package com.lishid.openinv.util.config;

import com.lishid.openinv.util.AccessEqualMode;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Config {

  private @NotNull Configuration root;
  private @Nullable AccessEqualMode accessEqualMode;

  public Config() {
    root = new MemoryConfiguration();
  }

  public void reload(@NotNull Configuration configuration) {
    root = configuration;
    accessEqualMode = null;
  }

  public boolean isSaveDisabled() {
    return root.getBoolean("settings.disable-saving", false);
  }

  public boolean isOfflineDisabled() {
    return root.getBoolean("settings.disable-offline-access", false);
  }

  public boolean doesNoArgsOpenSelf() {
    return root.getBoolean("settings.command.open.no-args-opens-self", false);
  }

  public @NotNull AccessEqualMode getAccessEqualMode() {
    if (accessEqualMode == null) {
      accessEqualMode = AccessEqualMode.of(root.getString("settings.equal-access"));
    }

    return accessEqualMode;
  }

}
