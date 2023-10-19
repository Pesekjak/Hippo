package me.pesekjak.hippo.core;

import org.objectweb.asm.ClassVisitor;

/**
 * Object that can be visited by a class visitor.
 */
public interface Visitable {

    /**
     * Visits this object.
     *
     * @param visitor visitor to use
     */
    void visit(ClassVisitor visitor);

}
