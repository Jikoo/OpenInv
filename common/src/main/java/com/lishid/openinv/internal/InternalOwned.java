package com.lishid.openinv.internal;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface InternalOwned<T> {

  T getOwnerHandle();

}
