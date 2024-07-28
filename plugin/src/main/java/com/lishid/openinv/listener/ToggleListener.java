package com.lishid.openinv.listener;

import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.util.setting.PlayerToggles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ToggleListener implements Listener {

  @Keep
  @EventHandler
  private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    PlayerToggles.any().set(playerId, false);
    PlayerToggles.silent().set(playerId, false);
  }

}
