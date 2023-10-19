package me.pesekjak.hippo.core;

import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Represents a parameter with annotations.
 */
public final class Parameter {

    private final Type type;
    private final Collection<Annotation> annotations;

    public Parameter(Type type, Collection<Annotation> annotations) {
        this.type = Objects.requireNonNull(type);
        this.annotations = Objects.requireNonNull(annotations);
    }

    public Parameter(String typeDescriptor, Collection<Annotation> annotations) {
        this(Type.getType(typeDescriptor), annotations);
    }

    /**
     * @return type of the parameter
     */
    public Type getType() {
        return type;
    }

    /**
     * @return annotations of the parameter
     */
    public @Unmodifiable Collection<Annotation> getAnnotations() {
        return Collections.unmodifiableCollection(annotations);
    }

}
