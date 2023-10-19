package me.pesekjak.hippo.utils;

import org.jetbrains.annotations.Nullable;

/**
 * Converter that casts to given type or returns null
 * if the object can not be casted to that type.
 */
public final class SafeCastConverter {

    private SafeCastConverter() {
        throw new UnsupportedOperationException();
    }

    public static @Nullable Object safeCast(Object o, Class<?> clazz) {
        if (o == null) return null;
        return clazz.isAssignableFrom(o.getClass()) ? o : null;
    }

}
