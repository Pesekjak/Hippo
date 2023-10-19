package me.pesekjak.hippo.core;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Utils related to operations with ASM types.
 */
public final class ASMUtil {

    private ASMUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns type from class dot path.
     *
     * @param dotPath dot path
     * @return type
     */
    public static Type getType(String dotPath) {
        return Type.getType(getDescriptor(dotPath));
    }

    /**
     * Finds a class for given type in the class loader.
     *
     * @param type type to search
     * @param classLoader class loader to search in
     * @return class
     * @throws ClassNotFoundException if there is no class for given type
     */
    public static Class<?> getClassFromType(Type type, ClassLoader classLoader) throws ClassNotFoundException {
        int arrayLevel = getArrayLevel(type);
        type = getTypeFromArrayType(type);
        Class<?> found;
        if (isPrimitive(type)) {
            found = switch (type.getDescriptor()) {
                case "Z" -> boolean.class;
                case "C" -> char.class;
                case "B" -> byte.class;
                case "S" -> short.class;
                case "I" -> int.class;
                case "F" -> float.class;
                case "J" -> long.class;
                case "D" -> double.class;
                default -> throw new IllegalArgumentException();
            };
        } else {
            found = classLoader.loadClass(type.getClassName());
        }
        for (int i = 0; i < arrayLevel; i++)
            found = found.arrayType();
        return found;
    }

    /**
     * Returns base component of given type or itself if the provided type
     * is not an array type.
     *
     * @param arrayType array type
     * @return base component
     */
    public static Type getTypeFromArrayType(Type arrayType) {
        if (!isArray(arrayType)) return arrayType;
        return Type.getType(arrayType.getDescriptor().replace("[", ""));
    }

    /**
     * Returns array type for given type.
     *
     * @param type type
     * @param level depth of the array
     * @return array type
     */
    public static Type getArrayType(Type type, int level) {
        return Type.getType(getArrayDescriptor(type, level));
    }

    /**
     * Returns array type for given dot path.
     *
     * @param dotPath dot path of the type
     * @param level depth of the array
     * @return array type
     */
    public static Type getArrayType(String dotPath, int level) {
        return Type.getType(getArrayDescriptor(dotPath, level));
    }

    /**
     * Returns array type for given class.
     *
     * @param clazz class
     * @param level depth of the array
     * @return array type
     */
    public static Type getArrayType(Class<?> clazz, int level) {
        return Type.getType(getArrayDescriptor(clazz, level));
    }

    /**
     * Returns array depth for given type.
     *
     * @param type type
     * @return array depth of given type
     */
    public static int getArrayLevel(Type type) {
        int level = 0;
        String descriptor = type.getDescriptor();
        while (descriptor.startsWith("[")) {
            level++;
            descriptor = descriptor.substring(1);
        }
        return level;
    }

    /**
     * Returns descriptor of given dot path.
     *
     * @param dotPath dot path
     * @return descriptor for given dot path
     */
    public static String getDescriptor(String dotPath) {
        return "L" + dotPath.replace(".", "/") + ";";
    }

    /**
     * Returns descriptor for given class.
     *
     * @param clazz class
     * @return descriptor for given class
     */
    public static String getDescriptor(Class<?> clazz) {
        return Type.getDescriptor(clazz);
    }

    /**
     * Returns array descriptor for given type.
     *
     * @param type type
     * @param level array depth
     * @return array descriptor
     */
    public static String getArrayDescriptor(Type type, int level) {
        return "[".repeat(Math.max(0, level))
                + type.getDescriptor();
    }

    /**
     * Returns array descriptor for given dot path.
     *
     * @param dotPath dot path
     * @param level array depth
     * @return array descriptor
     */
    public static String getArrayDescriptor(String dotPath, int level) {
        return "[".repeat(Math.max(0, level))
                + getDescriptor(dotPath);
    }

    /**
     * Returns array descriptor for given class.
     *
     * @param clazz class
     * @param level array depth
     * @return array descriptor
     */
    public static String getArrayDescriptor(Class<?> clazz, int level) {
        return "[".repeat(Math.max(0, level))
                + getDescriptor(clazz);
    }

    /**
     * Returns internal class name for a dot path.
     *
     * @param dotPath dot path
     * @return internal class name for the given dot path
     */
    public static String getInternalName(String dotPath) {
        return dotPath.replace(".", "/");
    }

    /**
     * Returns internal class name for a class.
     *
     * @param clazz class
     * @return internal class name for the given class
     */
    public static String getInternalName(Class<?> clazz) {
        return Type.getInternalName(clazz);
    }

    /**
     * Returns internal class name of an array type.
     *
     * @param type type
     * @param level array depth
     * @return internal class name for the array type
     */
    public static String getArrayInternalName(Type type, int level) {
        return getArrayDescriptor(type, level);
    }

    /**
     * Returns internal class name of an array type.
     *
     * @param dotPath dot path
     * @param level array depth
     * @return internal class name for the array type
     */
    public static String getArrayInternalName(String dotPath, int level) {
        return getArrayDescriptor(dotPath, level);
    }

    /**
     * Returns internal class name of an array type.
     *
     * @param clazz class
     * @param level array depth
     * @return internal class name for the array type
     */
    public static String getArrayInternalName(Class<?> clazz, int level) {
        return getArrayDescriptor(clazz, level);
    }

    /**
     * @param type type to check
     * @return whether the given type is primitive
     */
    public static boolean isPrimitive(Type type) {
        return !isComplex(type);
    }

    /**
     * @param type type to check
     * @return whether the given type is complex
     */
    public static boolean isComplex(Type type) {
        return type.getDescriptor().endsWith(";");
    }

    /**
     * @param type type to check
     * @return whether the given type is an array type
     */
    public static boolean isArray(Type type) {
        return type.getDescriptor().startsWith("[");
    }

    /**
     * Pushes new constant on the top of the stack.
     *
     * @param methodVisitor method visitor
     * @param constant constant to push
     */
    public static void pushConstant(final MethodVisitor methodVisitor, Object constant) {
        if (constant instanceof String || constant instanceof Type) {
            methodVisitor.visitLdcInsn(constant);
            return;
        }

        if (constant instanceof Float f) {
            if (f == 0) methodVisitor.visitInsn(Opcodes.FCONST_0);
            else if (f == 1) methodVisitor.visitInsn(Opcodes.FCONST_1);
            else if (f == 2) methodVisitor.visitInsn(Opcodes.FCONST_2);
            else methodVisitor.visitLdcInsn(f);
            return;
        }

        if (constant instanceof Double d) {
            if (d == 0) methodVisitor.visitInsn(Opcodes.DCONST_0);
            else if (d == 1) methodVisitor.visitInsn(Opcodes.DCONST_1);
            else methodVisitor.visitLdcInsn(d);
            return;
        }

        long value = 0;

        if (constant instanceof Boolean b)
            value = b ? 1 : 0;
        else if (constant instanceof Character c)
            value = c;
        else if (constant instanceof Integer i)
            value = i;
        else if (constant instanceof Long l)
            value = l;

        if (0 <= value && value <= 5)
            methodVisitor.visitInsn((int) (Opcodes.ICONST_0 + value));

        else if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE)
            methodVisitor.visitIntInsn(Opcodes.BIPUSH, (int) value);

        else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE)
            methodVisitor.visitIntInsn(Opcodes.SIPUSH, (int) value);

        else
            methodVisitor.visitLdcInsn(value);
    }

    /**
     * @param type type to check
     * @return whether the given type is number primitive.
     */
    public static boolean isNumberPrimitive(Type type) {
        String descriptor = type.getDescriptor();
        char last = descriptor.charAt(descriptor.length() - 1);
        return switch (last) {
            case 'B', 'S', 'I', 'F', 'J', 'D' -> true;
            default -> false;
        };
    }

}
