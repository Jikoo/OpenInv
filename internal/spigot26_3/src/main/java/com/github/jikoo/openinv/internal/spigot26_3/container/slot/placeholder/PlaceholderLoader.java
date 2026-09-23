package com.github.jikoo.openinv.internal.spigot26_3.container.slot.placeholder;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlaceholderLoader extends PlaceholderLoaderBase {

  @Override
  protected Item getWhiteBanner() {
    return Items.BANNER.white();
  }

  @Override
  protected Item getWhiteGlassPane() {
    return Items.STAINED_GLASS_PANE.white();
  }

}
