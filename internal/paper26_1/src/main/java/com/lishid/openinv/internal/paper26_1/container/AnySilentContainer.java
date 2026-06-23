package com.lishid.openinv.internal.paper26_1.container;

import com.lishid.openinv.util.lang.LanguageManager;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class AnySilentContainer extends com.lishid.openinv.internal.paper26_2.container.AnySilentContainer {

  public AnySilentContainer(@NotNull Logger logger, @NotNull LanguageManager lang) {
    super(logger, lang);
  }

  @Override
  protected boolean hasLootTable(@NotNull RandomizableContainerBlockEntity lootable) {
    return lootable.lootTable != null;
  }

}
