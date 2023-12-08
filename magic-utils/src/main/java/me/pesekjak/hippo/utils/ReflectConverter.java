package me.pesekjak.hippo.utils;

import com.btk5h.skriptmirror.Null;
import com.btk5h.skriptmirror.ObjectWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Converter that unwraps reflect objects and use JavaUtil conversion if applicable.
 */
public final class ReflectConverter {

    private ReflectConverter() {
        throw new UnsupportedOperationException();
    }

    public static Object unwrapIfNecessary(Object o) {
        return ObjectWrapper.unwrapIfNecessary(o);
    }

    public static @Nullable Object nullIfNecessary(Object o) {
        return o instanceof Null ? null : o;
    }

    public static @Nullable Object handle(Object o, Class<?> target) {
        try {
            if (JavaUtil.canConvert(o, target)) return JavaUtil.convert(o, target);
        } catch (Throwable ignored) { }
        return nullIfNecessary(unwrapIfNecessary(o));
    }

}
