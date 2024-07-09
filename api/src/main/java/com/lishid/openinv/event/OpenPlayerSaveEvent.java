package com.lishid.openinv.event;

import com.google.errorprone.annotations.RestrictedApi;
import com.lishid.openinv.internal.ISpecialInventory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired before OpenInv saves a player's data when closing an {@link ISpecialInventory}.
 */
public class OpenPlayerSaveEvent extends PlayerSaveEvent {

  private final ISpecialInventory inventory;

  /**
   * Construct a new {@code OpenPlayerSaveEvent}.
   *
   * <p>The constructor is not considered part of the API, and may be subject to change.</p>
   *
   * @param player the player to be saved
   * @param inventory the {@link ISpecialInventory} being closed
   */
  @RestrictedApi(
      explanation = "Constructor is not considered part of the API and may be subject to change.",
      link = "",
      allowedOnPath = ".*/com/lishid/openinv/event/OpenEvents.java")
  @ApiStatus.Internal
  OpenPlayerSaveEvent(@NotNull Player player, @NotNull ISpecialInventory inventory) {
    super(player);
    this.inventory = inventory;
  }

  /**
   * Get the {@link ISpecialInventory} that triggered the save by being closed.
   *
   * @return the special inventory
   */
  public @NotNull ISpecialInventory getInventory() {
    return inventory;
  }

}
