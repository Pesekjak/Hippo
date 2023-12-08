package me.pesekjak.hippo.utils;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import com.btk5h.skriptmirror.JavaType;
import com.btk5h.skriptmirror.Null;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Utils for easier conversion between Skript and Java types.
 */
// Java Utils
// Source: https://github.com/SkriptLang/skript-reflect/blob/2.x/src/main/java/com/btk5h/skriptmirror/util/JavaUtil.java
// License: https://github.com/SkriptLang/skript-reflect/blob/2.x/LICENSE
public final class JavaUtil {

    private static final Map<Class<?>, Class<?>> WRAPPER_CLASSES = new HashMap<>();
    private static final Set<Class<?>> NUMERIC_CLASSES = new HashSet<>();

    static {
        WRAPPER_CLASSES.put(boolean.class, Boolean.class);
        WRAPPER_CLASSES.put(byte.class, Byte.class);
        WRAPPER_CLASSES.put(char.class, Character.class);
        WRAPPER_CLASSES.put(double.class, Double.class);
        WRAPPER_CLASSES.put(float.class, Float.class);
        WRAPPER_CLASSES.put(int.class, Integer.class);
        WRAPPER_CLASSES.put(long.class, Long.class);
        WRAPPER_CLASSES.put(short.class, Short.class);

        NUMERIC_CLASSES.add(byte.class);
        NUMERIC_CLASSES.add(double.class);
        NUMERIC_CLASSES.add(float.class);
        NUMERIC_CLASSES.add(int.class);
        NUMERIC_CLASSES.add(long.class);
        NUMERIC_CLASSES.add(short.class);
        NUMERIC_CLASSES.add(Byte.class);
        NUMERIC_CLASSES.add(Double.class);
        NUMERIC_CLASSES.add(Float.class);
        NUMERIC_CLASSES.add(Integer.class);
        NUMERIC_CLASSES.add(Long.class);
        NUMERIC_CLASSES.add(Short.class);
    }

    private JavaUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Boxes primitive array.
     *
     * @param obj array to box
     * @return boxed array
     */
    public static Object boxPrimitiveArray(Object obj) {
        Class<?> componentType = obj.getClass().getComponentType();
        if (componentType != null && componentType.isPrimitive()) {
            int length = Array.getLength(obj);
            Object[] boxedArray = (Object[]) Array.newInstance(WRAPPER_CLASSES.get(componentType), length);

            for (int i = 0; i < length; i++)
                boxedArray[i] = Array.get(obj, i);

            obj = boxedArray;
        }
        return obj;
    }

    /**
     * Converts numeric array to numeric array of different class.
     *
     * @param array array to convert
     * @param to target type
     * @return converted numeric array
     */
    public static Object convertNumericArray(Object array, Class<?> to) {
        Class<?> componentType = array.getClass().getComponentType();
        int length = Array.getLength(array);

        Object newArray;

        if (componentType.isArray()) {
            newArray = Array.newInstance(componentType, length);

            for (int i = 0; i < length; i++) {
                Object innerArray = Array.get(array, i);

                if (innerArray == null) {
                    innerArray = Array.newInstance(componentType.getComponentType(), 0);
                    Array.set(newArray, i, innerArray);
                } else {
                    Array.set(newArray, i, convertNumericArray(innerArray, to));
                }
            }
        } else {
            newArray = Array.newInstance(to, length);

            for (int i = 0; i < length; i++) {
                Object what = Array.get(array, i);

                if (to == byte.class || to == Byte.class) {
                    what = ((Number) what).byteValue();
                } else if (to == double.class || to == Double.class) {
                    what = ((Number) what).doubleValue();
                } else if (to == float.class || to == Float.class) {
                    what = ((Number) what).floatValue();
                } else if (to == int.class || to == Integer.class) {
                    what = ((Number) what).intValue();
                } else if (to == long.class || to == Long.class) {
                    what = ((Number) what).longValue();
                } else if (to == short.class || to == Short.class) {
                    what = ((Number) what).shortValue();
                }

                Array.set(newArray, i, what);
            }
        }

        return newArray;
    }

    /**
     * @param clazz array class
     * @return depth of the array class
     */
    public static int getArrayDepth(Class<?> clazz) {
        int depth = 0;
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
            depth++;
        }
        return depth;
    }

    /**
     * @param obj array instance
     * @return base component class of the array
     */
    public static Class<?> getBaseComponent(Class<?> obj) {
        Class<?> componentType = obj.getComponentType();
        while (componentType.isArray())
            componentType = componentType.getComponentType();
        return componentType;
    }

    /**
     * @param clazz class to check
     * @return whether the class is numeric type
     */
    public static boolean isNumericClass(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz) || JavaUtil.NUMERIC_CLASSES.contains(clazz);
    }

    /**
     * Creates array class with given depth.
     *
     * @param type type of the array
     * @param layers depth of the array
     * @return array class
     */
    public static Class<?> getArrayClass(Class<?> type, int layers) {
        for (int i = 0; i < layers; i++)
            type = type.arrayType();
        return type;
    }

    /**
     * Checks whether an object can be converted to a different type.
     *
     * @param object object to convert
     * @param to target type
     * @return whether the object can be converted
     */
    public static boolean canConvert(Object object, Class<?> to) {
        if (to.isInstance(object)) {
            return true;
        }

        if (object instanceof Number && JavaUtil.NUMERIC_CLASSES.contains(to)) {
            return true;
        }

        if (to.isArray() && JavaUtil.getArrayDepth(to) == JavaUtil.getArrayDepth(object.getClass())) {
            Class<?> paramComponent = JavaUtil.getBaseComponent(to);
            Class<?> argComponent = JavaUtil.getBaseComponent(object.getClass());

            if (JavaUtil.isNumericClass(paramComponent) && JavaUtil.isNumericClass(argComponent)) {
                return true;
            }
        }

        if (to.isPrimitive() && JavaUtil.WRAPPER_CLASSES.get(to).isInstance(object)) {
            return true;
        }

        if (object instanceof String
                && (to == char.class || to == Character.class)
                && ((String) object).length() == 1) {
            return true;
        }

        if (object instanceof ItemType && to == ItemStack.class) {
            return true;
        }

        if (to == Class.class && (object instanceof JavaType || object instanceof ClassInfo)) {
            return true;
        }

        return !to.isPrimitive() && object instanceof Null;
    }

    /**
     * Converts an object to given type.
     *
     * @param object object to convert
     * @param to target type
     * @return converted object
     */
    public static Object convert(Object object, Class<?> to) {

        if (object instanceof Number && JavaUtil.NUMERIC_CLASSES.contains(to)) {
            if (to == byte.class || to == Byte.class) {
                return ((Number) object).byteValue();
            } else if (to == double.class || to == Double.class) {
                return ((Number) object).doubleValue();
            } else if (to == float.class || to == Float.class) {
                return ((Number) object).floatValue();
            } else if (to == int.class || to == Integer.class) {
                return ((Number) object).intValue();
            } else if (to == long.class || to == Long.class) {
                return ((Number) object).longValue();
            } else if (to == short.class || to == Short.class) {
                return ((Number) object).shortValue();
            }
        }

        if (to.isArray()
                && JavaUtil.getArrayDepth(to) == JavaUtil.getArrayDepth(object.getClass())
                && JavaUtil.isNumericClass(JavaUtil.getBaseComponent(to))) {
            return JavaUtil.convertNumericArray(object, JavaUtil.getBaseComponent(to));
        }

        if (object instanceof String && (to == char.class || to == Character.class)) {
            return ((String) object).charAt(0);
        }

        if (object instanceof ItemType && to == ItemStack.class) {
            return ((ItemType) object).getRandom();
        }

        if (to == Class.class) {
            if (object instanceof JavaType) {
                return ((JavaType) object).getJavaClass();
            } else if (object instanceof ClassInfo) {
                return ((ClassInfo<?>) object).getC();
            }
        }

        // unwrap null wrapper
        if (object instanceof Null) {
            return null;
        }

        return object;
    }

    /**
     * Returns the first common class for given classes in the class hierarchy.
     * <p>
     * Is used for searching varargs methods from arguments.
     *
     * @param classes classes to find the class for
     * @return first common class
     */
    public static Class<?> getClosestSuperClass(Class<?>... classes) {
        List<List<Class<?>>> trees = new ArrayList<>();
        for (int i = 0; i < classes.length - 1; i++) {
            ArrayList<Class<?>> tree = new ArrayList<>();
            Class<?> clazz = classes[i];
            while (clazz != null) {
                tree.add(clazz);
                clazz = clazz.getSuperclass();
            }
            trees.add(tree);
        }

        Class<?> lastClazz = classes[classes.length - 1];
        int[] cursors = new int[trees.size()];
        int matched = 0;

        do {
            for (int i = 0; i < trees.size(); i++) {
                if (cursors[i] == -1) continue;
                Class<?> compare = trees.get(i).get(cursors[i]);
                if (lastClazz.isAssignableFrom(compare)) {
                    cursors[i] = -1;
                    matched++;
                    continue;
                }
                cursors[i]++;
                lastClazz = lastClazz.getSuperclass();
                if (lastClazz == null) return Object.class;
            }
        } while (matched != cursors.length);

        return lastClazz;
    }

}
