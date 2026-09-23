package com.lishid.openinv.event;

import com.google.errorprone.annotations.RestrictedApi;
import com.lishid.openinv.util.setting.PlayerToggle;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * Event fired after OpenInv modifies a toggleable setting for a player.
 */
@NullMarked
public class PlayerToggledEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final PlayerToggle toggle;
  private final UUID uuid;
  private final boolean enabled;

  @RestrictedApi(
      explanation = "Constructor is not considered part of the API and may be subject to change.",
      link = "",
      allowedOnPath = ".*/com/lishid/openinv/event/OpenEvents.java"
  )
  @ApiStatus.Internal
  PlayerToggledEvent(PlayerToggle toggle, UUID uuid, boolean enabled) {
    this.toggle = toggle;
    this.uuid = uuid;
    this.enabled = enabled;
  }

  /**
   * Get the {@link PlayerToggle} affected.
   *
   * @return the toggle
   */
  public PlayerToggle getToggle() {
    return toggle;
  }

  /**
   * Get the {@link UUID} of the player whose setting was changed.
   *
   * @return the player ID
   */
  public UUID getPlayerId() {
    return uuid;
  }

  /**
   * Get whether the toggle is enabled.
   *
   * @return true if the toggle is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

}
