package com.github.jikoo.openinv.lang;

import org.jspecify.annotations.NullMarked;

/**
 * A data holder for string replacement in translations.
 *
 * @param placeholder the placeholder to be replaced
 * @param value the value to insert
 */
@NullMarked
public record Replacement(String placeholder, String value) {

}
