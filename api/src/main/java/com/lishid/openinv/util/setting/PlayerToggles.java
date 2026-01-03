package com.lishid.openinv.util.setting;

import com.lishid.openinv.util.Permissions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class containing all of OpenInv's {@link PlayerToggle PlayerToggles}.
 */
public final class PlayerToggles {

  private static final Map<String, PlayerToggle> TOGGLES = new HashMap<>();
  private static final PlayerToggle ANY = add(new MemoryToggle("AnyContainer") {
    @Override
    public boolean is(@NotNull Player player) {
      return is(player.getUniqueId())
          && (Permissions.CONTAINER_ANY.hasPermission(player) || Permissions.CONTAINER_ANY_USE.hasPermission(player));
    }
  });
  private static final PlayerToggle SILENT = add(new MemoryToggle("SilentContainer") {
    @Override
    public boolean is(@NotNull Player player) {
      return is(player.getUniqueId())
          && (Permissions.CONTAINER_SILENT.hasPermission(player) || Permissions.CONTAINER_SILENT_USE.hasPermission(player));
    }
  });

  /**
   * Get the AnyContainer toggle.
   *
   * @return the AnyContainer toggle
   */
  public static @NotNull PlayerToggle any() {
    return ANY;
  }

  /**
   * Get the SilentContainer toggle.
   *
   * @return the SilentContainer toggle
   */
  public static @NotNull PlayerToggle silent() {
    return SILENT;
  }

  /**
   * Get a toggle by name.
   *
   * @param toggleName the name of the toggle
   * @return the toggle, or null if no such toggle exists.
   */
  public static @Nullable PlayerToggle get(@NotNull String toggleName) {
    PlayerToggle toggle = TOGGLES.get(toggleName);
    if (toggle == null) {
      toggle = TOGGLES.get(toggleName.toLowerCase(Locale.ENGLISH));
    }
    return toggle;
  }

  /**
   * Get an unmodifable view of all toggles available.
   *
   * @return a view of all toggles available
   */
  public static @UnmodifiableView @NotNull Collection<PlayerToggle> get() {
    return Collections.unmodifiableCollection(TOGGLES.values());
  }

  private static @NotNull PlayerToggle add(@NotNull PlayerToggle toggle) {
    TOGGLES.put(toggle.getName().toLowerCase(Locale.ENGLISH), toggle);
    return toggle;
  }

  private PlayerToggles() {
    throw new IllegalStateException("Cannot create instance of utility class.");
  }

  private static abstract class MemoryToggle implements PlayerToggle {

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
