package com.lishid.openinv.util.setting;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A per-player setting that may be enabled or disabled.
 */
public interface PlayerToggle {

  /**
   * Get the name of the setting.
   *
   * @return the setting name
   */
  @NotNull String getName();

  /**
   * Get the state of the toggle for a particular player ID.
   *
   * @param uuid the player ID
   * @return true if the setting is enabled
   */
  boolean is(@NotNull UUID uuid);

  /**
   * Get the state of the toggle for a particular {@link Player},
   * accounting for permissions required to use the feature.
   *
   * @param player the player
   * @return true if the setting is enabled and the player has the required permissions
   */
  boolean is(@NotNull Player player);

  /**
   * Set the state of the toggle for a particular player ID.
   *
   * @param uuid the player ID
   * @param enabled whether the setting is enabled
   * @return true if the setting changed as a result of being set
   */
  boolean set(@NotNull UUID uuid, boolean enabled);

}
