package me.pesekjak.hippo.classes;

public class PrimitiveType extends IType {

    private final Primitive primitive;

    public PrimitiveType(String descriptor) {
        this.descriptor = descriptor;
        this.primitive = Primitive.fromDescriptor(getRawDescriptor());
    }

    public PrimitiveType(Primitive primitive) {
        this(primitive.getDescriptor());
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    public Primitive getPrimitive() {
        return primitive;
    }

    @Override
    public PrimitiveType arrayType() {
        return new PrimitiveType("[" + descriptor);
    }

    @Override
    public PrimitiveType varArgType() {
        PrimitiveType type = new PrimitiveType(descriptor);
        type.setVarArg(true);
        return type;
    }

    @Override
    public String getRawDescriptor() {
        if(descriptor == null) return "null";
        return descriptor.replace("[", "");
    }

}
