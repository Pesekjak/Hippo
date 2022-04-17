package me.pesekjak.hippo.hooks;

import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.classes.builder.DynamicClassLoader;
import me.pesekjak.hippo.utils.Reflectness;

import java.net.URLClassLoader;

public class SkriptReflectHook {

    private static Class<?> javaTypeClass = null;
    private static Class<?> objectWrapperClass = null;
    private static DynamicClassLoader libraryLoader = null;

    public static boolean setup() {
        try {
            javaTypeClass = Class.forName("com.btk5h.skriptmirror.JavaType");
            objectWrapperClass = Class.forName("com.btk5h.skriptmirror.ObjectWrapper");
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    public static void setupReflectLoader() {
        try {
            Class<?> reflectLoaderClass = Class.forName("com.btk5h.skriptmirror.LibraryLoader");
            URLClassLoader reflectLoader = (URLClassLoader) Reflectness.invoke(Reflectness.getMethod(reflectLoaderClass, "getClassLoader"), null);
            libraryLoader = new DynamicClassLoader(reflectLoader);
            Reflectness.setField(Reflectness.getField("classLoader", reflectLoaderClass), null, libraryLoader);
        } catch (Exception e) {
            libraryLoader = new DynamicClassLoader(Hippo.class.getClassLoader());
        }
    }

    public static Class<?> getJavaTypeClass() {
        return javaTypeClass;
    }

    public static Class<?> getObjectWrapperClass() {
        return objectWrapperClass;
    }

    public static DynamicClassLoader getLibraryLoader() {
        return libraryLoader;
    }

    public static Class<?> classOfJavaType(Object javaTypeObject) {
        return (Class<?>) Reflectness.invoke(Reflectness.getMethod(javaTypeClass, "getJavaClass"), javaTypeObject);
    }

    public static Object buildJavaType(Class<?> classInstance) {
        Object javaType = null;
        if(classInstance == null) return null;
        try {
            javaType = Reflectness.newInstance(Reflectness.getConstructor(javaTypeClass, Class.class), classInstance);
        } catch (Exception ignored) { }
        return javaType;
    }

    public static Object unwrap(Object object) {
        return Reflectness.invoke(Reflectness.getMethod(objectWrapperClass, "unwrapIfNecessary", Object.class), null, object);
    }

}
