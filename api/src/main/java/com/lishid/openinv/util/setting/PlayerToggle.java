package com.lishid.openinv.util.setting;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * A per-player setting that may be enabled or disabled.
 */
@NullMarked
public interface PlayerToggle {

  /**
   * Get the name of the setting.
   *
   * @return the setting name
   */
  String getName();

  /**
   * Get the state of the toggle for a particular player ID.
   *
   * @param uuid the player ID
   * @return true if the setting is enabled
   */
  boolean is(UUID uuid);

  /**
   * Get the state of the toggle for a particular {@link Player},
   * accounting for permissions required to use the feature.
   *
   * @param player the player
   * @return true if the setting is enabled and the player has the required permissions
   */
  boolean is(Player player);

  /**
   * Set the state of the toggle for a particular player ID.
   *
   * @param uuid the player ID
   * @param enabled whether the setting is enabled
   * @return true if the setting changed as a result of being set
   */
  boolean set(UUID uuid, boolean enabled);

}
