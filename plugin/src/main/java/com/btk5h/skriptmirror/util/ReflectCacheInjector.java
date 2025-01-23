package com.btk5h.skriptmirror.util;

import com.btk5h.skriptmirror.SkriptMirror;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

public final class ReflectCacheInjector {

    private ReflectCacheInjector() {
        throw new UnsupportedOperationException();
    }

    public static void inject() throws IllegalAccessException, IOException {
        byte[] classData;
        try (InputStream is = ReflectCacheInjector.class.getResourceAsStream("LRUCache.class")) {
            classData = Objects.requireNonNull(is).readAllBytes();
        }

        Thread thread = Thread.currentThread();
        ClassLoader old = thread.getContextClassLoader();
        thread.setContextClassLoader(SkriptMirror.class.getClassLoader());
        MethodHandles.privateLookupIn(SkriptMirrorUtil.class, MethodHandles.lookup()).defineClass(classData);
        thread.setContextClassLoader(old);
    }

}
