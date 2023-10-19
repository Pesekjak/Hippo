package me.pesekjak.hippo.utils;

import com.btk5h.skriptmirror.Null;
import com.btk5h.skriptmirror.ObjectWrapper;
import me.pesekjak.hippo.core.ASMUtil;
import me.pesekjak.hippo.core.loader.DynamicClassLoader;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

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

    public static @Nullable Object handle(Object o, String targetDescriptor) {
        try {
            Class<?> found = ASMUtil.getClassFromType(Type.getType(targetDescriptor), DynamicClassLoader.getInstance());
            if (JavaUtil.canConvert(o, found)) return JavaUtil.convert(o, found);
        } catch (Throwable ignored) { }
        return nullIfNecessary(unwrapIfNecessary(o));
    }

}
