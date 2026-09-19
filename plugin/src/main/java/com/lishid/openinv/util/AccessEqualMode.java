package com.lishid.openinv.util;

import com.lishid.openinv.util.config.Config;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;

@NullMarked
public enum AccessEqualMode {

  DENY, ALLOW, VIEW;

  public static AccessEqualMode of(@Nullable String value) {
    if (value == null) {
      return VIEW;
    }
    return switch (value.toLowerCase(Locale.ENGLISH)) {
      case "deny", "false" -> DENY;
      case "allow", "true" -> ALLOW;
      default -> VIEW;
    };
  }

  public static AccessEqualMode getByPerm(Permissible permissible, Config config) {
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
