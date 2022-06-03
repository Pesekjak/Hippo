package me.pesekjak.hippo.hooks;

import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.DynamicClassLoader;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.Reflectness;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Map;

public class SkriptReflectHook {

    private static Class<?> javaTypeClass = null;
    private static Class<?> objectWrapperClass = null;
    private static Class<?> reflectNullClass = null;
    private static Class<?> customImportClass = null;
    private static DynamicClassLoader libraryLoader = null;

    public static boolean setup() {
        try {
            javaTypeClass = Class.forName("com.btk5h.skriptmirror.JavaType");
            objectWrapperClass = Class.forName("com.btk5h.skriptmirror.ObjectWrapper");
            reflectNullClass = Class.forName("com.btk5h.skriptmirror.Null");
            customImportClass = Class.forName("com.btk5h.skriptmirror.skript.custom.CustomImport");
            SkriptReflectHook.setupReflectLoader();
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

    public static Class<?> getReflectNullClass() {
        return reflectNullClass;
    }

    public static DynamicClassLoader getLibraryLoader() {
        return libraryLoader;
    }

    public static Class<?> classOfJavaType(Object javaTypeObject) {
        if(!SkriptReflectHook.getJavaTypeClass().isInstance(javaTypeObject)) {
            if(javaTypeObject instanceof Type) {
                Logger.severe("Type " + ((Type) javaTypeObject).getDotPath() + " found at place where JavaType was expected. Make sure your pre-imported classes exist (or were compiled without problems).");
            } else {
                Logger.severe(javaTypeObject.toString() + " (" + javaTypeObject.getClass().getName() + ") isn't supported as JavaType by Hippo, make sure that class alias doesn't match with different expression. In that case you can use JavaType Wrapper Expression.");
            }
            return null;
        }
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

    public static Map<File, Map<String, Object>> getReflectCustomImportsMap() {
        return (Map<File, Map<String, Object>>) Reflectness.getField(Reflectness.getField("imports", customImportClass), null);
    }

}
