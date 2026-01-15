package com.lishid.openinv.util.profile;

import com.github.jikoo.planarwrappers.scheduler.TickTimeUnit;
import me.nahu.scheduler.wrapper.WrappedJavaPlugin;
import me.nahu.scheduler.wrapper.runnable.WrappedRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class BatchProfileStore implements ProfileStore {

  private final Set<Profile> pending = Collections.synchronizedSet(new HashSet<>());
  private final AtomicReference<WrappedRunnable> insertTask = new AtomicReference<>();
  protected final @NotNull WrappedJavaPlugin plugin;

  protected BatchProfileStore(@NotNull WrappedJavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void addProfile(@NotNull Profile profile) {
    pending.add(profile);
    buildBatch();
  }

  private void buildBatch() {
    if (insertTask.compareAndSet(null, new WrappedRunnable() {
      @Override
      public void run() {
        pushBatch();
      }
    })) {
      try {
        // Wait 5 seconds to accumulate other player data to reduce scheduler load on larger servers.
        insertTask.get().runTaskLaterAsynchronously(plugin, TickTimeUnit.toTicks(5, TimeUnit.SECONDS));
      } catch (IllegalStateException e) {
        // If scheduling task fails, server is most likely shutting down.
        insertTask.set(null);
      }
    }
  }

  private void pushBatch() {
    Set<Profile> batch = new HashSet<>(pending);
    // This is a bit roundabout but removes the risk of data loss.
    pending.removeAll(batch);

    // Push current batch.
    pushBatch(batch);

    WrappedRunnable running = insertTask.getAndSet(null);
    if (running != null) {
      running.cancel();
    }

    // If more profiles have been added, build another batch.
    if (!pending.isEmpty()) {
      buildBatch();
    }
  }

  @Override
  public void shutdown() {
    WrappedRunnable wrappedRunnable = insertTask.get();
    if (wrappedRunnable != null) {
      wrappedRunnable.cancel();
    }
    pushBatch();
    insertTask.set(null);
  }

  protected abstract void pushBatch(@NotNull Set<Profile> batch);

}
