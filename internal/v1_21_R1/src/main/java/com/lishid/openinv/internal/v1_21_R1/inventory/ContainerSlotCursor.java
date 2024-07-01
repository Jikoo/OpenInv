package com.lishid.openinv.internal.v1_21_R1.inventory;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.bukkit.craftbukkit.v1_21_R1.CraftRegistry;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A slot wrapping the active menu's cursor. Unavailable when not online in a survival mode.
 */
class ContainerSlotCursor implements ContainerSlot {

  private static final ItemStack PLACEHOLDER;

  static {
    PLACEHOLDER = new ItemStack(Items.WHITE_BANNER);
    RegistryAccess minecraftRegistry = CraftRegistry.getMinecraftRegistry();
    Registry<BannerPattern> bannerPatterns = minecraftRegistry.registryOrThrow(Registries.BANNER_PATTERN);
    BannerPattern halfDiagBottomRight = bannerPatterns.getOrThrow(BannerPatterns.DIAGONAL_RIGHT);
    BannerPattern downRight = bannerPatterns.getOrThrow(BannerPatterns.STRIPE_DOWNRIGHT);
    BannerPattern border = bannerPatterns.getOrThrow(BannerPatterns.BORDER);
    PLACEHOLDER.set(DataComponents.BANNER_PATTERNS,
        new BannerPatternLayers(List.of(
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(halfDiagBottomRight), DyeColor.GRAY),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(downRight), DyeColor.WHITE),
            new BannerPatternLayers.Layer(bannerPatterns.wrapAsHolder(border), DyeColor.GRAY))));
    PLACEHOLDER.set(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE);
  }

  private @NotNull ServerPlayer holder;

  ContainerSlotCursor(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public void setHolder(@NotNull ServerPlayer holder) {
    this.holder = holder;
  }

  @Override
  public ItemStack get() {
    return isAvailable() ? holder.containerMenu.getCarried() : ItemStack.EMPTY;
  }

  @Override
  public ItemStack remove() {
    ItemStack carried = holder.containerMenu.getCarried();
    holder.containerMenu.setCarried(ItemStack.EMPTY);
    return carried;
  }

  @Override
  public ItemStack removePartial(int amount) {
    ItemStack carried = holder.containerMenu.getCarried();
    if (!carried.isEmpty() && carried.getCount() >= amount) {
      ItemStack value = carried.split(amount);
      if (carried.isEmpty()) {
        holder.containerMenu.setCarried(ItemStack.EMPTY);
      }
      return value;
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void set(ItemStack itemStack) {
    if (isAvailable()) {
      holder.containerMenu.setCarried(itemStack);
    } else {
      holder.drop(itemStack, false);
    }
  }

  private boolean isAvailable() {
    // Player must be online and not in creative - since the creative client is (semi-)authoritative,
    // it ignores changes without extra help, and will delete the item as a result.
    // Spectator mode is technically possible but may cause the item to be dropped if the client opens an inventory.
    return holder.connection != null && !holder.connection.isDisconnected() && holder.gameMode.isSurvival();
  }

  @Override
  public Slot asMenuSlot(Container container, int index, int x, int y) {
    return new SlotCursor(container, index, x, y);
  }

  @Override
  public InventoryType.SlotType getSlotType() {
    // As close as possible to "not real"
    return InventoryType.SlotType.OUTSIDE;
  }

  class SlotCursor extends MenuSlotPlaceholder {

    private SlotCursor(Container container, int index, int x, int y) {
      super(container, index, x, y);
    }

    @Override
    ItemStack getOrDefault() {
      if (!isAvailable()) {
        return survivalOnly(holder);
      }
      ItemStack carried = holder.containerMenu.getCarried();
      return carried.isEmpty() ? PLACEHOLDER : carried;
    }

    @Override
    public boolean mayPickup(Player player) {
      return isAvailable();
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
      return isAvailable();
    }

    @Override
    public boolean hasItem() {
      return isAvailable() && super.hasItem();
    }

    @Override
    public boolean isFake() {
      return true;
    }

  }

}
