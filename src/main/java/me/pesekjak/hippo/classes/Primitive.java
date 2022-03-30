package me.pesekjak.hippo.classes;

public enum Primitive {
    BOOLEAN("boolean", "Z"),
    CHAR("char", "C"),
    BYTE("byte", "B"),
    SHORT("short", "S"),
    INT("int", "I"),
    FLOAT("float", "F"),
    LONG("long", "J"),
    DOUBLE("double", "D"),
    VOID("void", "V"),
    NONE(null, null);

    private final String primitive;
    private final String descriptor;

    Primitive(String primitive, String descriptor) {
        this.primitive = primitive;
        this.descriptor = descriptor;
    }

    public String getPrimitive() {
        return primitive;
    }

    public String getDescriptor() {
        return descriptor;
    }
}
