package me.pesekjak.hippo.core.classes;

import me.pesekjak.hippo.core.*;
import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Collection;

/**
 * Represents an interface class.
 */
public class Interface extends AbstractClass {

    public Interface(@Nullable AbstractClass outerClass,
                     Type type,
                     Collection<Type> interfaces,
                     int modifier,
                     Collection<Annotation> annotations) throws IllegalModifiersException {
        super(
                outerClass,
                type,
                Type.getType(Object.class),
                interfaces,
                modifier | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
                annotations
        );
    }

    @Override
    protected void checkField(Field field) throws IllegalClassContentException {
        if (!field.isStatic())
            throw new IllegalClassContentException("Field '" + field.getName() + "' needs to be static");
        if (field.isEnum())
            throw new IllegalClassContentException("Field '" + field.getName() + "' cannot be enum");
        if (field.isPrivate())
            throw new IllegalClassContentException("Field '" + field.getName() + "' cannot be private");
        if (field.isProtected())
            throw new IllegalClassContentException("Field '" + field.getName() + "' cannot be protected");
    }

    @Override
    protected void checkConstructor(Constructor constructor) throws IllegalClassContentException {
        throw new IllegalClassContentException("Interfaces cannot have constructors");
    }

    @Override
    protected void checkMethod(Method method) throws IllegalClassContentException {
        if (method.isPrivate())
            throw new IllegalClassContentException("Method '" + method.getName() + "' cannot be private");
        if (method.isProtected())
            throw new IllegalClassContentException("Method '" + method.getName() + "' cannot be protected");
        if (method.isAbstract() && method.isStatic())
            throw new IllegalClassContentException("Method '" + method.getName() + "' is abstract and static at the same time");
        if (method.isAbstract() && method.isSynchronized())
            throw new IllegalClassContentException("Method '" + method.getName() + "' is abstract and synchronized at the same time");
        if (method.isAbstract() && method.isStrict())
            throw new IllegalClassContentException("Method '" + method.getName() + "' is abstract and strict at the same time");
        if (method.isAbstract() && method.isNative())
            throw new IllegalClassContentException("Method '" + method.getName() + "' is abstract and native at the same time");
    }

    @Override
    public void visitOuterInit(Constructor constructor, MethodVisitor visitor) {

    }

    @Override
    public void visitOuterClinit(Method method, MethodVisitor visitor) {

    }

    @Override
    public void visitInit(Constructor constructor, MethodVisitor visitor) {

    }

    @Override
    public void visitClinit(Method method, MethodVisitor visitor) {

    }

}
