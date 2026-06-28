package com.github.jikoo.openinv.internal.spigot26_1.player;

import net.minecraft.server.PlayerAdvancements;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public class PlayerManager extends com.github.jikoo.openinv.internal.spigot26_2.player.PlayerManager {

  public PlayerManager(Logger logger) {
    super(logger);
  }

  @Override
  protected void removeListeners(PlayerAdvancements advancements) {
    advancements.stopListening();
  }

}
