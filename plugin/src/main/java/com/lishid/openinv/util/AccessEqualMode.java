package com.lishid.openinv.util;

import com.lishid.openinv.util.config.Config;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum AccessEqualMode {

  DENY, ALLOW, VIEW;

  public static @NotNull AccessEqualMode of(@Nullable String value) {
    if (value == null) {
      return VIEW;
    }
    return switch (value.toLowerCase(Locale.ENGLISH)) {
      case "deny", "false" -> DENY;
      case "allow", "true" -> ALLOW;
      default -> VIEW;
    };
  }

  public static @NotNull AccessEqualMode getByPerm(@NotNull Permissible permissible, @NotNull Config config) {
    if (Permissions.ACCESS_EQUAL_EDIT.hasPermission(permissible)) {
      return AccessEqualMode.ALLOW;
    }
    if (Permissions.ACCESS_EQUAL_VIEW.hasPermission(permissible)) {
      return AccessEqualMode.VIEW;
    }
    if (Permissions.ACCESS_EQUAL_DENY.hasPermission(permissible)) {
      return AccessEqualMode.DENY;
    }
    return config.getAccessEqualMode();
  }

}
