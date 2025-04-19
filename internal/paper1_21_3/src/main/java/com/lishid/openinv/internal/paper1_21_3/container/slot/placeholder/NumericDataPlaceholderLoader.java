package com.lishid.openinv.internal.paper1_21_3.container.slot.placeholder;

import com.lishid.openinv.internal.paper1_21_4.container.slot.placeholder.PlaceholderLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.jetbrains.annotations.NotNull;

public class NumericDataPlaceholderLoader extends PlaceholderLoader {

  private static final CustomModelData DEFAULT_CUSTOM_MODEL_DATA = new CustomModelData(9999);

  @Override
  protected void addModelData(@NotNull ItemStack itemStack) {
    itemStack.set(DataComponents.CUSTOM_MODEL_DATA, DEFAULT_CUSTOM_MODEL_DATA);
  }

}
