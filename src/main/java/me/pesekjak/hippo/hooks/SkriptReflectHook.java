package me.pesekjak.hippo.hooks;

import me.pesekjak.hippo.utils.Reflectness;

public class SkriptReflectHook {

    private static Class<?> javaTypeClass = null;

    public static boolean setup() {
        try {
            javaTypeClass = Class.forName("com.btk5h.skriptmirror.JavaType");
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    public static Class<?> getJavaTypeClass() {
        return javaTypeClass;
    }

    public static Class<?> classOfJavaType(Object javaTypeObject) {
        return (Class<?>) Reflectness.invoke(Reflectness.getMethod(javaTypeClass, "getJavaClass"), javaTypeObject);
    }

    public static Object buildJavaType(Class<?> classInstance) {
        Object javaType = null;
        try {
            javaType = Reflectness.newInstance(Reflectness.getConstructor(javaTypeClass, Class.class), classInstance);
        } catch (Exception ignored) { }
        return javaType;
    }

}
