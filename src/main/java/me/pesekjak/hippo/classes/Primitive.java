package me.pesekjak.hippo.classes;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public enum Primitive {

    BOOLEAN(boolean.class, Boolean.class, Type.BOOLEAN_TYPE),
    CHAR(char.class, Character.class, Type.CHAR_TYPE),
    BYTE(byte.class, Byte.class, Type.BYTE_TYPE),
    SHORT(short.class, Short.class, Type.SHORT_TYPE),
    INT(int.class, Integer.class, Type.INT_TYPE),
    LONG(long.class, Long.class, Type.LONG_TYPE),
    FLOAT(float.class, Float.class, Type.FLOAT_TYPE),
    DOUBLE(double.class, Double.class, Type.DOUBLE_TYPE),
    VOID(void.class, Void.class, Type.VOID_TYPE);

    @Getter @NotNull
    private final Class<?> primitiveClass;
    @Getter @NotNull
    private final Class<?> nonPrimitiveClass;
    @Getter @NotNull
    private final String descriptor;
    @Getter @NotNull
    private final Type ASMType;

    Primitive(@NotNull Class<?> primitiveClass, @NotNull Class<?> nonPrimitiveClass, Type ASMType) {
        this.primitiveClass = primitiveClass;
        this.nonPrimitiveClass = nonPrimitiveClass;
        this.descriptor = ASMType.getDescriptor();
        this.ASMType = ASMType;
    }

    public static Primitive fromClass(Class<?> primitiveClass) {
        for(Primitive primitive : Primitive.values()) {
            if(primitive.getPrimitiveClass() == primitiveClass ||
                    primitive.getNonPrimitiveClass() == primitiveClass)
                return primitive;
        }
        return null;
    }

}
