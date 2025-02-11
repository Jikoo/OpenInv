package com.lishid.openinv.internal.common.container.slot.placeholder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

public class PlaceholderLoader extends PlaceholderLoaderBase {

  private static final CustomModelData DEFAULT_CUSTOM_MODEL_DATA = new CustomModelData(List.of(), List.of(), List.of("openinv:custom"), List.of());

  @Override
  protected void addModelData(ItemStack itemStack) {
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
  }

}
