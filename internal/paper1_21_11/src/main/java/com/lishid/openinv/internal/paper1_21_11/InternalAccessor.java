package com.lishid.openinv.internal.paper1_21_11;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.paper1_21_11.container.OpenEnderChest;
import com.lishid.openinv.internal.paper1_21_11.container.OpenInventory;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.logging.Logger;

@NullMarked
public class InternalAccessor extends com.lishid.openinv.internal.paper26_1.InternalAccessor {

  public InternalAccessor(Logger logger, LanguageManager lang) {
    super(logger, lang);
  }

  @Override
  public ISpecialEnderChest createEnderChest(Player player) {
    return new OpenEnderChest(player);
  }

  @Override
  public ISpecialPlayerInventory createPlayerInventory(Player player) {
    return new OpenInventory(player);
  }

}
