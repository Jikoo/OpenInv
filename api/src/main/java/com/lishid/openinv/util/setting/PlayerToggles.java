package com.lishid.openinv.util.setting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerToggles {

  private static final Map<String, PlayerToggle> TOGGLES = new HashMap<>();
  private static final PlayerToggle ANY = add(new MemoryToggle("anycontainer"));
  private static final PlayerToggle SILENT = add(new MemoryToggle("silentcontainer"));

  public static @NotNull PlayerToggle any() {
    return ANY;
  }

  public static @NotNull PlayerToggle silent() {
    return SILENT;
  }

  public static @Nullable PlayerToggle get(@NotNull String toggleName) {
    return TOGGLES.get(toggleName);
  }

  public static @UnmodifiableView @NotNull Collection<PlayerToggle> get() {
    return Collections.unmodifiableCollection(TOGGLES.values());
  }

  private static @NotNull PlayerToggle add(@NotNull PlayerToggle toggle) {
    TOGGLES.put(toggle.getName(), toggle);
    return toggle;
  }

  private PlayerToggles() {
    throw new IllegalStateException("Cannot create instance of utility class.");
  }

  private static class MemoryToggle implements PlayerToggle {

    private final @NotNull Set<UUID> enabled;
    private final @NotNull String name;

    private MemoryToggle(@NotNull String name) {
      enabled = new HashSet<>();
      this.name = name;
    }

    @Override
    public @NotNull String getName() {
      return this.name;
    }

    @Override
    public boolean is(@NotNull UUID uuid) {
      return enabled.contains(uuid);
    }

    @Override
    public boolean set(@NotNull UUID uuid, boolean enabled) {
      if (enabled) {
        return this.enabled.add(uuid);
      } else {
        return this.enabled.remove(uuid);
      }
    }

  }

}
