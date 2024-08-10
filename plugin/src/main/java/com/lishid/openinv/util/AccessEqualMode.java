package com.lishid.openinv.util;

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

}
