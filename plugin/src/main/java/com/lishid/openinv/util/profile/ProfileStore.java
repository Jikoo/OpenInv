package com.lishid.openinv.util.profile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

public interface ProfileStore {

  void addProfile(@NotNull Profile profile);

  void shutdown();

  @Nullable Profile getProfileExact(@NotNull String name);

  @NotNull @Unmodifiable Collection<Profile> getProfiles(@NotNull String search);

}
