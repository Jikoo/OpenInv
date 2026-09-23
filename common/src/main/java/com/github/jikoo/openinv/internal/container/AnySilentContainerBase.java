package com.github.jikoo.openinv.internal.container;

import com.lishid.openinv.internal.IAnySilentContainer;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class AnySilentContainerBase implements IAnySilentContainer {

  public enum StateType {
    ENDER_CHEST,
    SHULKER_BOX,
    CHEST,
    BARREL,
    OTHER
  }

  @Override
  public boolean isAnyContainerNeeded(Block block) {
    return isAnyContainerNeeded(getStateType(block), block);
  }

  protected boolean isAnyContainerNeeded(StateType material, Block block) {
    return switch (material) {
      // Enderchests require a non-occluding block on top to open.
      case ENDER_CHEST -> block.getRelative(0, 1, 0).getType().isOccluding();
      // Shulker boxes require half a block clear in the direction they open.
      case SHULKER_BOX -> isShulkerBlocked(block);
      // Chests require that the block above not be occluding or occupied by a cat.
      case CHEST -> isChestBlocked(block);
      // Barrels need silent but not any. All others unsupported.
      default -> false;
    };
  }

  @Override
  public boolean isChestBlocked(Block block) {
    if (IAnySilentContainer.super.isChestBlocked(block)) {
      return true;
    }

    BlockData blockData = block.getBlockData();
    if (!(blockData instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE) {
      return false;
    }

    BlockFace relativeFace = switch (chest.getFacing()) {
      case NORTH -> chest.getType() == Chest.Type.RIGHT ? BlockFace.WEST : BlockFace.EAST;
      case EAST -> chest.getType() == Chest.Type.RIGHT ? BlockFace.NORTH : BlockFace.SOUTH;
      case SOUTH -> chest.getType() == Chest.Type.RIGHT ? BlockFace.EAST : BlockFace.WEST;
      case WEST -> chest.getType() == Chest.Type.RIGHT ? BlockFace.SOUTH : BlockFace.NORTH;
      default -> BlockFace.SELF;
    };
    Block relative = block.getRelative(relativeFace);

    if (relative.getType() != block.getType()) {
      return false;
    }

    BlockData relativeData = relative.getBlockData();
    if (!(relativeData instanceof Chest relativeChest)) {
      return false;
    }

    if (relativeChest.getFacing() != chest.getFacing()
        || relativeChest.getType() != (chest.getType() == Chest.Type.RIGHT ? Chest.Type.LEFT : Chest.Type.RIGHT)) {
      return false;
    }

    return IAnySilentContainer.super.isChestBlocked(relative);
  }

  @Override
  public boolean isAnySilentContainer(Block block) {
    return isAnySilentContainer(getState(block));
  }

  @Override
  public boolean isAnySilentContainer(Inventory inventory) {
    return isAnySilentContainer(getHolder(inventory));
  }

  protected StateType getStateType(Block block) {
    return switch (getState(block)) {
      case Barrel ignored -> StateType.BARREL;
      case EnderChest ignored -> StateType.ENDER_CHEST;
      case ShulkerBox ignored -> StateType.SHULKER_BOX;
      case org.bukkit.block.Chest ignored -> StateType.CHEST;
      default -> StateType.OTHER;
    };
  }

  protected abstract BlockState getState(Block block);

  protected abstract @Nullable InventoryHolder getHolder(Inventory inventory);

}
