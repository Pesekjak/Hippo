package me.pesekjak.hippo.core.annotations;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;

import java.util.Objects;

/**
 * Represents a literal value.
 */
public final class Constant implements AnnotationVisitable {

    private final Object value;

    private Constant(Object value) {
        this.value = Objects.requireNonNull(value);
    }

    public Constant(Byte value) {
        this((Object) value);
    }

    public Constant(Boolean value) {
        this((Object) value);
    }

    public Constant(Character value) {
        this((Object) value);
    }

    public Constant(Short value) {
        this((Object) value);
    }

    public Constant(Integer value) {
        this((Object) value);
    }

    public Constant(Long value) {
        this((Object) value);
    }

    public Constant(Float value) {
        this((Object) value);
    }

    public Constant(Double value) {
        this((Object) value);
    }

    public Constant(String value) {
        this((Object) value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get() {
        return (T) value;
    }

    @Override
    public void visit(@Nullable String name, AnnotationVisitor visitor) {
        visitor.visit(name, value);
    }

}
