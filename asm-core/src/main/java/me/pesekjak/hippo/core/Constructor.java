package me.pesekjak.hippo.core;

import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Represents a class constructor.
 */
public final class Constructor extends Method {

    private BiConsumer<Constructor, MethodVisitor> superWriter;

    public Constructor(AbstractClass source,
                       Collection<Parameter> argumentTypes,
                       int modifier,
                       Collection<Annotation> annotations,
                       Collection<Type> exceptions) throws IllegalModifiersException {
        super(
                source,
                Constants.CONSTRUCTOR_METHOD_NAME,
                new Parameter(Type.VOID_TYPE, Collections.emptyList()),
                argumentTypes,
                modifier,
                annotations,
                exceptions
        );
    }

    @Override
    public void visit(ClassVisitor visitor) {
        String[] exceptions = getExceptions().stream().map(Type::getInternalName).toArray(String[]::new);
        MethodVisitor methodVisitor = visitor.visitMethod(getModifier(), getName(), getDescriptor(), null, exceptions);

        // Visiting constructor and parameter annotations
        getAnnotations().forEach(annotation -> annotation.visit(methodVisitor));
        List<Parameter> parameters = getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            for (Annotation annotation : parameters.get(i).getAnnotations())
                annotation.visitMethodParam(i, methodVisitor);
        }

        methodVisitor.visitCode();

        // Accepting the super writer
        if (getSuperWriter() != null) getSuperWriter().accept(this, methodVisitor);

        // Visiting source class
        assert getSource() != null;
        getSource().visitInit(this, methodVisitor);

        // Visiting content
        Collection<ClassContent> contents = getSource().getContents();
        contents.forEach(content -> {
            if (content instanceof AbstractClass innerClass)
                innerClass.visitOuterInit(this, methodVisitor);
            else
                content.visitInit(this, methodVisitor);
        });

        // Accepting the instructions writer
        if (getWriter() != null) getWriter().accept(this, methodVisitor);

        methodVisitor.visitInsn(Opcodes.RETURN); // Constructors should always have void type
        methodVisitor.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        methodVisitor.visitEnd();
        visitor.visitEnd();
    }

    @Override
    public int getCompatibleModifiers() {
        return Modifiers.getConstructorModifiers();
    }

    /**
     * @return super constructor call bytecode writer
     */
    public @Nullable BiConsumer<Constructor, MethodVisitor> getSuperWriter() {
        return superWriter;
    }

    /**
     * @param superWriter new super constructor call bytecode writer
     */
    public void setSuperWriter(@Nullable BiConsumer<Constructor, MethodVisitor> superWriter) {
        this.superWriter = superWriter;
    }


}
