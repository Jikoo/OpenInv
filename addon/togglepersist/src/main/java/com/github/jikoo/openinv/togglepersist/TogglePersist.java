package com.github.jikoo.openinv.togglepersist;

import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.event.PlayerToggledEvent;
import com.lishid.openinv.util.setting.PlayerToggle;
import com.lishid.openinv.util.setting.PlayerToggles;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class TogglePersist extends JavaPlugin implements Listener {

  private final Map<UUID, Set<String>> enabledToggles = new HashMap<>();

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    File file = new File(getDataFolder(), "toggles.yml");

    // If there's no save file, there's nothing to load.
    if (!file.exists()) {
      return;
    }

    Configuration loaded = YamlConfiguration.loadConfiguration(file);

    // For each toggle, enable loaded players.
    for (String toggleName : loaded.getKeys(false)) {
      PlayerToggle toggle = PlayerToggles.get(toggleName);
      // Ensure toggle exists.
      if (toggle == null) {
        continue;
      }

      for (String idString : loaded.getStringList(toggleName)) {
        // Ensure valid UUID.
        UUID uuid;
        try {
          uuid = UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
          continue;
        }

        // Track that toggle is enabled.
        set(uuid, toggleName);
      }
    }
  }

  private void set(UUID playerId, String toggleName) {
    enabledToggles.compute(playerId, (uuid, toggles) -> {
      if (toggles == null) {
        toggles = new HashSet<>();
      }
      toggles.add(toggleName);
      return toggles;
    });
  }

  @Override
  public void onDisable() {
    Map<String, List<String>> converted = getSaveData();

    YamlConfiguration data = new YamlConfiguration();
    for (Map.Entry<String, List<String>> playerToggle : converted.entrySet()) {
      data.set(playerToggle.getKey(), playerToggle.getValue());
    }

    File file = new File(getDataFolder(), "toggles.yml");
    try {
      data.save(file);
    } catch (IOException e) {
      getLogger().log(Level.SEVERE, "Unable to save player toggle states", e);
    }
  }

  private @NotNull Map<String, List<String>> getSaveData() {
    Map<String, List<String>> converted = new HashMap<>();

    for (Map.Entry<UUID, Set<String>> playerToggles : enabledToggles.entrySet()) {
      String idString = playerToggles.getKey().toString();
      for (String toggleName : playerToggles.getValue()) {
        // Add player ID to listing for each enabled toggle.
        converted.compute(toggleName, (name, ids) -> {
          if (ids == null) {
            ids = new ArrayList<>();
          }
          ids.add(idString);
          return ids;
        });
      }
    }
    return converted;
  }

  @Keep
  @EventHandler
  private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    Set<String> toggleNames = enabledToggles.get(playerId);

    if (toggleNames == null) {
      return;
    }

    for (String toggleName : toggleNames) {
      PlayerToggle toggle = PlayerToggles.get(toggleName);
      if (toggle != null) {
        toggle.set(playerId, true);
      }
    }
  }

  @Keep
  @EventHandler
  private void onToggleSet(@NotNull PlayerToggledEvent event) {
    if (event.isEnabled()) {
      set(event.getPlayerId(), event.getToggle().getName());
    } else {
      enabledToggles.computeIfPresent(event.getPlayerId(), (uuid, toggles) -> {
        toggles.remove(event.getToggle().getName());
        return toggles.isEmpty() ? null : toggles;
      });
    }
  }

}
