package me.pesekjak.hippo.classes;

public class Argument {

    private final PrimitiveType primitiveType;
    private final String name;
    private Type type;

    public Argument(PrimitiveType primitive, Type type, String name) {
        this.primitiveType = primitive;
        this.name = name;
        this.type = type;
    }

    public Argument(PrimitiveType primitive, String name) {
        this.primitiveType = primitive;
        this.name = name;
    }

    public Argument(Type type, String name) {
        this(new PrimitiveType(Primitive.NONE), name);
        this.type = type;
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

//    public String toJavaCode() {
//        return (type != null ? type.getJavaClassPath() : primitive.getPrimitive()) + " " + name;
//    }
}
