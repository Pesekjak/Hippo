package me.pesekjak.hippo.classes;

import org.objectweb.asm.Opcodes;

public enum Primitive {
    BOOLEAN("boolean", "Z", Opcodes.T_BOOLEAN),
    CHAR("char", "C", Opcodes.T_CHAR),
    BYTE("byte", "B", Opcodes.T_BYTE),
    SHORT("short", "S", Opcodes.T_SHORT),
    INT("int", "I", Opcodes.T_INT),
    FLOAT("float", "F", Opcodes.T_FLOAT),
    LONG("long", "J", Opcodes.T_LONG),
    DOUBLE("double", "D", Opcodes.T_DOUBLE),
    VOID("void", "V", 0),
    NONE(null, null, 0);

    private final String primitive;
    private final String descriptor;
    private final int typeValue;

    Primitive(String primitive, String descriptor, int typeValue) {
        this.primitive = primitive;
        this.descriptor = descriptor;
        this.typeValue = typeValue;
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
}
