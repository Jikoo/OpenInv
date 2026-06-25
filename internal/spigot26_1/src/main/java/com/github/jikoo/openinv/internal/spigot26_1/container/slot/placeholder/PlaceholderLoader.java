package com.github.jikoo.openinv.internal.spigot26_1.container.slot.placeholder;

import com.github.jikoo.openinv.internal.spigot26_2.container.slot.placeholder.PlaceholderLoaderBase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlaceholderLoader extends PlaceholderLoaderBase {

  @Override
  protected Item getWhiteBanner() {
    return Items.WHITE_BANNER;
  }

  @Override
  protected Item getWhiteGlassPane() {
    return Items.WHITE_STAINED_GLASS_PANE;
  }

}
