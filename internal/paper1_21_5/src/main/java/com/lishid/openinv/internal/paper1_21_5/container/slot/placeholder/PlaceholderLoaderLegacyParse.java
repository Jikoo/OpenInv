package com.lishid.openinv.internal.paper1_21_5.container.slot.placeholder;

import com.lishid.openinv.internal.common.container.slot.placeholder.PlaceholderLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PlaceholderLoaderLegacyParse extends PlaceholderLoader {

  @Override
  protected @NotNull ItemStack parse(
      @Nullable ConfigurationSection section,
      @NotNull String path,
      @NotNull ItemStack defaultStack
  ) throws Exception {
    if (section == null) {
      return defaultStack;
    }

    String itemText = section.getString(path);

    if (itemText == null) {
      return defaultStack;
    }

    CompoundTag compoundTag = parseTag(itemText);
    Optional<ItemStack> parsed = ItemStack.parse(CraftRegistry.getMinecraftRegistry(), compoundTag);
    return parsed.filter(itemStack -> !itemStack.isEmpty()).orElse(defaultStack);
  }

}
