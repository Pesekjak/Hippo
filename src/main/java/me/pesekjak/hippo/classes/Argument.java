package me.pesekjak.hippo.classes;

public class Argument {

    private final Primitive primitive;
    private final String name;
    private Type type;

    public Argument(Primitive primitive, Type type, String name) {
        this.primitive = primitive;
        this.name = name;
        this.type = type;
    }

    public Argument(Primitive primitive, String name) {
        this.primitive = primitive;
        this.name = name;
    }

    public Argument(Type type, String name) {
        this(Primitive.NONE, name);
        this.type = type;
    }

    public Primitive getPrimitive() {
        return primitive;
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
