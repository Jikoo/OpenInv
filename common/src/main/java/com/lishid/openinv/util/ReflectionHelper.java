/*
 * Copyright (C) 2011-2021 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.util;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * A utility for making reflection easier.
 */
public final class ReflectionHelper {

    /**
     * Grab an {@link Object} stored in a {@link Field} of another {@code Object}.
     *
     * <p>This casts the field to the correct class. Any issues will result in a {@code null} return value.
     *
     * @param fieldType the {@link Class} of {@code Object} stored in the {@code Field}
     * @param holder the containing {@code Object}
     * @param <T> the type of stored {@code Object}
     * @return the first matching {@code Object} or {@code null} if none match
     */
    public static <T> @Nullable T grabObjectByType(final Object holder, final Class<T> fieldType) {
        Field field = grabFieldByType(holder.getClass(), fieldType);

        if (field != null) {
            try {
                return fieldType.cast(field.get(holder));
            } catch (IllegalAccessException ignored) {
                // Ignore issues obtaining field
            }
        }

        return null;
    }

    /**
     * Grab a {@link Field} of an {@link Object}
     *
     * @param fieldType the {@link Class} of the object
     * @param holderType the containing {@code Class}
     * @return the first matching object or {@code null} if none match
     */
    public static @Nullable Field grabFieldByType(Class<?> holderType, Class<?> fieldType) {
        for (Field field : holderType.getDeclaredFields()) {
            if (fieldType.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return field;
            }
        }

        if (holderType.getSuperclass() != null) {
            return grabFieldByType(fieldType, holderType.getSuperclass());
        }

        return null;
    }

    private ReflectionHelper() {
        throw new IllegalStateException("Cannot create instance of utility class.");
    }

}
