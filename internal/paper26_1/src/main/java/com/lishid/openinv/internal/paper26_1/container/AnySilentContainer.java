package com.lishid.openinv.internal.paper26_1.container;

import com.lishid.openinv.util.lang.LanguageManager;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public class AnySilentContainer extends com.lishid.openinv.internal.paper26_3.container.AnySilentContainer {

  public AnySilentContainer(Logger logger, LanguageManager lang) {
    super(logger, lang);
  }

  @Override
  protected boolean hasLootTable(RandomizableContainerBlockEntity lootable) {
    return lootable.lootTable != null;
  }

}
