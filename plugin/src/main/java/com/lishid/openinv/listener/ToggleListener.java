package com.lishid.openinv.listener;

import com.lishid.openinv.util.setting.Toggles;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ToggleListener implements Listener {

  @EventHandler
  private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    Toggles.any().set(playerId, false);
    Toggles.silent().set(playerId, false);
  }

}
