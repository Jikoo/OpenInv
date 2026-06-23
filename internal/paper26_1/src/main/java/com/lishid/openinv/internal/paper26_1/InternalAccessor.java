package com.lishid.openinv.internal.paper26_1;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.paper26_1.container.AnySilentContainer;
import com.lishid.openinv.internal.paper26_1.container.OpenEnderChest;
import com.lishid.openinv.internal.paper26_1.container.slot.placeholder.PlaceholderLoader;
import com.lishid.openinv.internal.paper26_1.player.PlayerManager;
import com.lishid.openinv.internal.paper26_2.container.slot.placeholder.PlaceholderLoaderBase;
import com.lishid.openinv.util.lang.LanguageManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class InternalAccessor extends com.lishid.openinv.internal.paper26_2.InternalAccessor {

  public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
    super(logger, lang);
  }

  @Override
  protected @NotNull PlayerManager createPlayerManager(@NotNull Logger logger) {
    return new PlayerManager(logger);
  }

  @Override
  protected @NotNull AnySilentContainer createAnySilentContainer(
      @NotNull Logger logger,
      @NotNull LanguageManager lang
  ) {
    return new AnySilentContainer(logger, lang);
  }

  @Override
  protected @NotNull PlaceholderLoaderBase createPlaceholderLoader() {
    return new PlaceholderLoader();
  }

  @Override
  public @NotNull ISpecialEnderChest createEnderChest(@NotNull Player player) {
    return new OpenEnderChest(player);
  }

}
