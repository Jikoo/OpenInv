package com.github.jikoo.openinv.internal.container;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface InternalOwned<T> {

  T getOwnerHandle();

}
