package me.pesekjak.hippo.core;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

/**
 * Represents a content of a class.
 */
public sealed interface ClassContent extends Visitable, Modifiable, Annotable permits AbstractClass, Field, Method {

    /**
     * @return source of the content
     */
    @Nullable AbstractClass getSource();

    /**
     * @return name of the content
     */
    String getName();

    /**
     * @return descriptor of the content
     */
    String getDescriptor();

    /**
     * Called when constructor of the source class is visited.
     *
     * @param constructor constructor that is being visited
     * @param visitor visitor
     */
    void visitInit(Constructor constructor, MethodVisitor visitor);

    /**
     * Called when static block of the source class is visited.
     *
     * @param method static block that is being visited
     * @param visitor visitor
     */
    void visitClinit(Method method, MethodVisitor visitor);

}
