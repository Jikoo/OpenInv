package com.github.jikoo.openinv.internal.spigot26_2.container.slot.placeholder;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class PlaceholderLoader extends PlaceholderLoaderBase {

  @Override
  protected @NotNull Item getWhiteBanner() {
    return Items.BANNER.white();
  }

  @Override
  protected @NotNull Item getWhiteGlassPane() {
    return Items.STAINED_GLASS_PANE.white();
  }

}
