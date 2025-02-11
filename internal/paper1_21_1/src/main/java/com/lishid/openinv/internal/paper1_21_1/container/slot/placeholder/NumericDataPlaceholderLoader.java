package com.lishid.openinv.internal.paper1_21_1.container.slot.placeholder;

import com.lishid.openinv.internal.common.container.slot.placeholder.PlaceholderLoaderBase;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

public class NumericDataPlaceholderLoader extends PlaceholderLoaderBase {

  private static final CustomModelData DEFAULT_CUSTOM_MODEL_DATA = new CustomModelData(9999);

  @Override
  protected void addModelData(ItemStack itemStack) {
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
  }

}
