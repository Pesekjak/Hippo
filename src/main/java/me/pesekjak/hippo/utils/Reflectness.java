package me.pesekjak.hippo.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflectness {

    public static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static boolean classExists(String className) {
        return classForName(className) != null;
    }


    public static void setField(Field field, Object obj, Object arg) {
        try {
            field.setAccessible(true);
            field.set(obj, arg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void setField(String name, Class<? extends T> theClass, T obj, Object arg) {
        Field field = getField(name, theClass);
        if (field != null) setField(field, obj, arg);
    }

    public static <T> void setField(String name, T obj, T arg) {
        setField(name, obj.getClass(), obj, arg);
    }


    public static Object getField(Field field, Object obj) {
        if (field.trySetAccessible())
            try { return field.get(obj); }
            catch (Exception ignore) {}
        return null;
    }


    public static <T> Object getField(String name, Class<? extends T> theClass, T obj) {
        Field field = getField(name, theClass);
        return field == null ? null : getField(field, obj);
    }

    public static Object getField(String name, Object obj) {
        return getField(name, obj.getClass(), obj);
    }

    private static final Table<Class<?>, String, Field> FIELDS = HashBasedTable.create();

    public static Field getField(String name, Class<?> theClass) {
        Field field = FIELDS.get(theClass, name);
        if (field == null)
            try {
                Field getField = theClass.getDeclaredField(name);
                FIELDS.put(theClass, name, getField);
                return getField;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        return field;
    }

    public static Method getMethod(Class<?> theClass, String name, Class<?>...args) {
        try {
            Method method = theClass.getDeclaredMethod(name, args);
            method.setAccessible(true);
            return method;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Object invoke(Method method, Object obj, Object... args) {
        try {
            if (method != null) {
                method.setAccessible(true);
                return method.invoke(obj, args);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static <T> Constructor<T> getConstructor(Class<T> theClass, Class<?>... args) {
        try {
            return theClass.getDeclaredConstructor(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


}
