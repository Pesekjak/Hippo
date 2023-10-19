package me.pesekjak.hippo.core.classes;

import me.pesekjak.hippo.core.*;
import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a classic class.
 */
public class Class extends AbstractClass {

    public Class(@Nullable AbstractClass outerClass,
                 Type type,
                 @Nullable Type superClass,
                 Collection<Type> interfaces,
                 int modifier,
                 Collection<Annotation> annotations) throws IllegalModifiersException {
        super(
                outerClass,
                type,
                superClass,
                interfaces,
                modifier,
                annotations
        );
    }

    @Override
    protected void checkField(Field field) throws IllegalClassContentException {
        if (field.isEnum())
            throw new IllegalClassContentException("Field '" + field.getName() + "' cannot be enum");
    }

    @Override
    protected void checkConstructor(Constructor constructor) {

    }

    @Override
    protected void checkMethod(Method method) throws IllegalClassContentException {
        if (method.isAbstract() && !isAbstract())
            throw new IllegalClassContentException("Method '" + method.getName() + "' cannot be abstract");
        if (method.isAbstract() && method.isPrivate())
            throw new IllegalClassContentException("Method '" + method.getName() + "' is abstract and private at the same time");
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

    @Override
    public Collection<ClassContent> getContents() {
        List<ClassContent> content = new ArrayList<>(super.getContents());
        if (getConstructors().size() != 0) return content;
        if (!getSuperClass().getDescriptor().equals(Type.getDescriptor(Object.class))) return content;

        assert getSource() != null;

        // Default constructor
        try {
            Constructor c = new Constructor(
                    this,
                    Collections.emptyList(),
                    Opcodes.ACC_PUBLIC,
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            content.add(c);
            c.setSuperWriter((constructor, methodVisitor) -> {
                assert constructor.getSource() != null;
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                methodVisitor.visitMethodInsn(
                        Opcodes.INVOKESPECIAL,
                        constructor.getSource().getSuperClass().getInternalName(),
                        Constants.CONSTRUCTOR_METHOD_NAME,
                        Type.getMethodDescriptor(Type.VOID_TYPE),
                        false);
            });
        } catch (IllegalModifiersException exception) {
            throw new RuntimeException(exception);
        }
        return content;
    }

}
