package com.lishid.openinv.internal.paper26_1.container.slot.placeholder;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class PlaceholderLoader extends com.lishid.openinv.internal.paper26_2.container.slot.placeholder.PlaceholderLoader {

  @Override
  protected @NotNull Item getDefaultCursorItem() {
    return Items.WHITE_BANNER;
  }

  @Override
  protected @NotNull Item getDefaultNotSlotItem() {
    return Items.WHITE_STAINED_GLASS_PANE;
  }

}
