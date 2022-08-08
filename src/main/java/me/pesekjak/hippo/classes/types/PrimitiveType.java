package me.pesekjak.hippo.classes.types;

import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PrimitiveType implements Type {

    protected final String descriptor;
    protected final Primitive primitive;

    protected PrimitiveType(String descriptor, Primitive primitive) {
        this.descriptor = descriptor;
        this.primitive = primitive;
    }

    public PrimitiveType(@NotNull Primitive primitive) {
        this(primitive.getDescriptor(), primitive);
    }

    @Override
    public @Nullable String dotPath() {
        return null;
    }

    @Override
    public @NotNull String descriptor() {
        return descriptor;
    }

    @Override
    public @Nullable String internalName() {
        return null;
    }

    @Override
    public @NotNull String simpleName() {
        return primitive.name().toLowerCase();
    }

    @Override
    public boolean isArray() {
        return descriptor.startsWith("[");
    }

    @Override
    public org.objectweb.asm.Type toASM() {
        return org.objectweb.asm.Type.getType(descriptor);
    }

    @Override
    public @Nullable Class<?> findClass() {
        return primitive.getPrimitiveClass();
    }

}
