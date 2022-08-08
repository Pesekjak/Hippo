package me.pesekjak.hippo.classes.builder;

import me.pesekjak.hippo.classes.ISkriptClass;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public interface IClassBuilder {

    /**
     * Starts building the class.
     */
    void build();

    /**
     * Returns the ClassVisitor used by the Builder.
     * @return ClassVisitor of the Builder
     */
    ClassVisitor visitor();

    /**
     * Returns the SkriptClass the ClassBuilder is building.
     * @return SkriptClass of the ClassBuilder
     */
    ISkriptClass skriptClass();

    /**
     * Pushes the SkriptClass of this ClassBuilder to the stack.
     */
    void pushClass(final MethodVisitor MV);

}
