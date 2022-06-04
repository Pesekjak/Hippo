package me.pesekjak.hippo.classes;

import org.objectweb.asm.Opcodes;

public enum Primitive {
    BOOLEAN("boolean", "Z", Opcodes.T_BOOLEAN, Boolean.class, boolean.class),
    CHAR("char", "C", Opcodes.T_CHAR, Character.class, char.class),
    BYTE("byte", "B", Opcodes.T_BYTE, Byte.class, byte.class),
    SHORT("short", "S", Opcodes.T_SHORT, Short.class, short.class),
    INT("int", "I", Opcodes.T_INT, Integer.class, int.class),
    FLOAT("float", "F", Opcodes.T_FLOAT, Float.class, float.class),
    LONG("long", "J", Opcodes.T_LONG, Long.class, long.class),
    DOUBLE("double", "D", Opcodes.T_DOUBLE, Double.class, double.class),
    VOID("void", "V", 0, Void.class, void.class),
    NONE(null, null, 0, null, null);

    private final String primitive;
    private final String descriptor;
    private final int typeValue;
    private final Class<?> classCounterpart;
    private final Class<?> primitiveClass;

    Primitive(String primitive, String descriptor, int typeValue, Class<?> classCounterpart, Class<?> primitiveClass) {
        this.primitive = primitive;
        this.descriptor = descriptor;
        this.typeValue = typeValue;
        this.classCounterpart = classCounterpart;
        this.primitiveClass = primitiveClass;
    }

    public static Primitive fromDescriptor(String descriptor) {
        return switch (descriptor) {
            case "Z" -> Primitive.BOOLEAN;
            case "C" -> Primitive.CHAR;
            case "B" -> Primitive.BYTE;
            case "S" -> Primitive.SHORT;
            case "I" -> Primitive.INT;
            case "F" -> Primitive.FLOAT;
            case "J" -> Primitive.LONG;
            case "D" -> Primitive.DOUBLE;
            case "V" -> Primitive.VOID;
            default -> Primitive.NONE;
        };
    }

    public String getPrimitive() {
        return primitive;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public int getTypeValue() {
        return typeValue;
    }

    public Class<?> getClassCounterpart() {
        return classCounterpart;
    }

    public Class<?> getPrimitiveClass() {
        return primitiveClass;
    }
}
