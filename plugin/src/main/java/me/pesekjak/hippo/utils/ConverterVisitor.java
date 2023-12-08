package me.pesekjak.hippo.utils;

import me.pesekjak.hippo.core.ASMUtil;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Util for writing instructions to convert values on the top of the stack.
 */
public final class ConverterVisitor {

    private ConverterVisitor() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts object on the top of the stack to given type. (non-primitive)
     *
     * @param methodVisitor method visitor
     * @param target target type
     */
    public static void convertTopObject(MethodVisitor methodVisitor, Type target) {
        if (target.getDescriptor().equals(Type.VOID_TYPE.getDescriptor())) return;

        visitReflectConverter(methodVisitor, target);

        if (ASMUtil.isPrimitive(target) && !ASMUtil.isArray(target)) {
            if (ASMUtil.isNumberPrimitive(target))
                visitPrimitiveNumberConverter(methodVisitor, target);
            else
                visitUnboxer(methodVisitor, target);
            return;
        }

        try {
            Class<?> clazz = Class.forName(target.getClassName());
            if (Number.class.isAssignableFrom(clazz) && !clazz.isArray()) visitNumberConverter(methodVisitor, target);
        } catch (ClassNotFoundException ignored) { }

        visitSafeCastConverter(methodVisitor, target);
        methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, target.getInternalName());
    }

    /**
     * Converts primitive on the top of the stack to given type. (non-primitive - boxing)
     *
     * @param methodVisitor method visitor
     * @param target target type
     */
    public static void convertTopPrimitive(MethodVisitor methodVisitor, Type target) {
        visitAutoBoxer(methodVisitor, target);
    }

    /**
     * Converts primitive on the top of the stack to its non-primitive counterpart.
     * @param methodVisitor method visitor
     * @param primitiveType primitive type to convert from
     */
    public static void convertTopPrimitiveToObject(MethodVisitor methodVisitor, Type primitiveType) {
        Type target = switch (primitiveType.getDescriptor().charAt(0)) {
            case 'Z' -> Type.getType(Boolean.class);
            case 'C' -> Type.getType(Character.class);
            case 'B' -> Type.getType(Byte.class);
            case 'S' -> Type.getType(Short.class);
            case 'I' -> Type.getType(Integer.class);
            case 'F' -> Type.getType(Float.class);
            case 'J' -> Type.getType(Long.class);
            case 'D' -> Type.getType(Double.class);
            default -> throw new IllegalArgumentException();
        };
        convertTopPrimitive(methodVisitor, target);
    }

    /**
     * Visits convertor that handles skript-reflect wrapper objects.
     *
     * @param methodVisitor method visitors
     * @param target target type
     */
    private static void visitReflectConverter(MethodVisitor methodVisitor, Type target) {
        if (ASMUtil.isPrimitive(target) && !ASMUtil.isArray(target)) {
            Type counterPart = switch (target.getDescriptor().charAt(0)) {
                case 'Z' -> Type.getType(Boolean.class);
                case 'C' -> Type.getType(Character.class);
                case 'B' -> Type.getType(Byte.class);
                case 'S' -> Type.getType(Short.class);
                case 'I' -> Type.getType(Integer.class);
                case 'F' -> Type.getType(Float.class);
                case 'J' -> Type.getType(Long.class);
                case 'D' -> Type.getType(Double.class);
                default -> throw new IllegalStateException();
            };
            methodVisitor.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    counterPart.getInternalName(),
                    "TYPE",
                    Type.getType(Class.class).getDescriptor()
            );
        } else {
            methodVisitor.visitLdcInsn(target);
        }
        methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getType(ReflectConverter.class).getInternalName(),
                "handle",
                Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class), Type.getType(Class.class)),
                false
        );
    }

    /**
     * Visits convertor that handles number types.
     *
     * @param methodVisitor method visitors.
     * @param target target type
     */
    private static void visitNumberConverter(MethodVisitor methodVisitor, Type target) {
        methodVisitor.visitLdcInsn(target);
        methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getType(NumberConverter.class).getInternalName(),
                "convertNumber",
                Type.getMethodDescriptor(Type.getType(Number.class), Type.getType(Object.class), Type.getType(Class.class)),
                false
        );
    }

    /**
     * Visits convertor that unboxes objects.
     *
     * @param methodVisitor method visitor
     * @param target target type
     */
    private static void visitUnboxer(MethodVisitor methodVisitor, Type target) {
        String methodName = switch (target.getDescriptor().charAt(0)) {
            case 'Z' -> "asBoolean";
            case 'C' -> "asChar";
            case 'B' -> "asByte";
            case 'S' -> "asShort";
            case 'I' -> "asInt";
            case 'F' -> "asFloat";
            case 'J' -> "asLong";
            case 'D' -> "asDouble";
            default -> throw new IllegalArgumentException();
        };
        methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getType(Unboxer.class).getInternalName(),
                methodName,
                Type.getMethodDescriptor(target, Type.getType(Object.class)),
                false
        );
    }

    /**
     * Visits convertor that boxes primitives.
     *
     * @param methodVisitor method visitor
     * @param target target type
     */
    private static void visitAutoBoxer(MethodVisitor methodVisitor, Type target) {
        Type primitiveCounter = switch (target.getDescriptor()) {
            case "Ljava/lang/Boolean;" -> Type.BOOLEAN_TYPE;
            case "Ljava/lang/Character;" -> Type.CHAR_TYPE;
            case "Ljava/lang/Byte;" -> Type.BYTE_TYPE;
            case "Ljava/lang/Short;" -> Type.SHORT_TYPE;
            case "Ljava/lang/Integer;" -> Type.INT_TYPE;
            case "Ljava/lang/Float;" -> Type.FLOAT_TYPE;
            case "Ljava/lang/Long;" -> Type.LONG_TYPE;
            case "Ljava/lang/Double;" -> Type.DOUBLE_TYPE;
            default -> throw new IllegalArgumentException();
        };
        methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getType(AutoBoxer.class).getInternalName(),
                "box",
                Type.getMethodDescriptor(target, primitiveCounter),
                false
        );
    }

    /**
     * Visits convertor that converts primitive numbers.
     *
     * @param methodVisitor method visitor
     * @param target target type
     */
    private static void visitPrimitiveNumberConverter(MethodVisitor methodVisitor, Type target) {
        String methodName = switch (target.getDescriptor().charAt(0)) {
            case 'B' -> "convertByte";
            case 'S' -> "convertShort";
            case 'I' -> "convertInt";
            case 'F' -> "convertFloat";
            case 'J' -> "convertLong";
            case 'D' -> "convertDouble";
            default -> throw new IllegalArgumentException();
        };
        methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getType(NumberConverter.class).getInternalName(),
                methodName,
                Type.getMethodDescriptor(target, Type.getType(Object.class)),
                false
        );
    }

    /**
     * Visits convertor that safely casts to target type or return null if
     * target class is not assignable from the class of object at the top
     * of the stack.
     *
     * @param methodVisitor method visitor
     * @param target target type
     */
    private static void visitSafeCastConverter(MethodVisitor methodVisitor, Type target) {
        methodVisitor.visitLdcInsn(target);
        methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getType(SafeCastConverter.class).getInternalName(),
                "safeCast",
                Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class), Type.getType(Class.class)),
                false
        );
    }

}
