package me.pesekjak.hippo.core.annotations;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents an array of literals.
 *
 * @param content literals
 */
public record LiteralsArray(Collection<AnnotationVisitable> content) implements AnnotationVisitable {

    public LiteralsArray {
        Objects.requireNonNull(content);
    }

    public LiteralsArray(AnnotationVisitable... content) {
        this(List.of(content));
    }

    @Override
    public void visit(@Nullable String name, AnnotationVisitor visitor) {
        AnnotationVisitor arrayVisitor = visitor.visitArray(name);
        content.forEach(content -> content.visit(null, arrayVisitor));
        arrayVisitor.visitEnd();
    }

}
