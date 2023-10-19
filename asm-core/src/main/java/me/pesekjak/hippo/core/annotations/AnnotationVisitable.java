package me.pesekjak.hippo.core.annotations;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;

/**
 * Represents an object that can be visited by the annotation visitor.
 */
public interface AnnotationVisitable {

    /**
     * Visits the object.
     *
     * @param name name of the element
     * @param visitor visitor
     */
    void visit(@Nullable String name, AnnotationVisitor visitor);

}
