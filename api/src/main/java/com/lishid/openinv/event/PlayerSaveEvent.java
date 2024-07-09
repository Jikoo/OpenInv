package com.lishid.openinv.event;


import com.google.errorprone.annotations.RestrictedApi;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired before a {@link Player} loaded via OpenInv is saved.
 */
public class PlayerSaveEvent extends PlayerEvent implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();

  private boolean cancelled = false;

  /**
   * Construct a new {@code PlayerSaveEvent}.
   *
   * <p>The constructor is not considered part of the API, and may be subject to change.</p>
   *
   * @param player the player to be saved
   */
  @RestrictedApi(
      explanation = "Constructor is not considered part of the API and may be subject to change.",
      link = "",
      allowedOnPath = ".*/com/lishid/openinv/event/(OpenPlayerSaveEvent|OpenEvents).java")
  @ApiStatus.Internal
  PlayerSaveEvent(@NotNull Player player) {
    super(player);
  }

  /**
   * Get whether the event is cancelled.
   *
   * @return true if the event is cancelled
   */
  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  /**
   * Set whether the event is cancelled.
   *
   * @param cancel whether the event is cancelled
   */
  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

}
