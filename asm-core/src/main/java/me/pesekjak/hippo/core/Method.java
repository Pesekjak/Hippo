package me.pesekjak.hippo.core;

import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.*;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Represents a class method.
 */
public sealed class Method implements ClassContent permits Constructor, StaticBlock {

    private final AbstractClass source;
    private final String name;
    private final Parameter returnType;
    private final List<Parameter> parameters;
    private int modifier;
    private final Collection<Annotation> annotations;
    private final Collection<Type> exceptions;

    private @Nullable BiConsumer<Method, MethodVisitor> writer;

    public Method(AbstractClass source,
                  String name,
                  Parameter returnType,
                  Collection<Parameter> parameters,
                  int modifier,
                  Collection<Annotation> annotations,
                  Collection<Type> exceptions) throws IllegalModifiersException {
        this.source = Objects.requireNonNull(source);
        this.name = Objects.requireNonNull(name);
        this.returnType = Objects.requireNonNull(returnType);
        this.parameters = new ArrayList<>(Objects.requireNonNull(parameters));
        this.modifier = modifier;
        this.annotations = Objects.requireNonNull(annotations);
        this.exceptions = Objects.requireNonNull(exceptions);
        Modifiable.checkModifiers(this, "Method '" + getName() + "'");
        for (Parameter parameter : parameters)
            if (parameter.getType().getDescriptor().equals("V")) throw new IllegalStateException();
    }

    @Override
    public @Unmodifiable Collection<Annotation> getAnnotations() {
        return Collections.unmodifiableCollection(annotations);
    }

    @Override
    public AbstractClass getSource() {
        return source;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescriptor() {
        return Type.getMethodDescriptor(returnType.getType(), parameters.stream().map(Parameter::getType).toArray(Type[]::new));
    }

    @Override
    public void visitInit(Constructor constructor, MethodVisitor visitor) {

    }

    @Override
    public void visitClinit(Method method, MethodVisitor visitor) {

    }

    @Override
    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        this.modifier = modifier;
    }

    @Override
    public int getCompatibleModifiers() {
        return Modifiers.getMethodModifiers();
    }

    @Override
    public void visit(ClassVisitor visitor) {
        String[] exceptions = getExceptions().stream().map(Type::getInternalName).toArray(String[]::new);
        MethodVisitor methodVisitor = visitor.visitMethod(getModifier(), getName(), getDescriptor(), null, exceptions);

        // Visiting method, return type, and parameter annotations
        getAnnotations().forEach(annotation -> annotation.visit(methodVisitor));
        getReturnType().getAnnotations().forEach(annotation -> annotation.visitType(TypeReference.METHOD_RETURN, null, methodVisitor));
        List<Parameter> parameters = getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            for (Annotation annotation : parameters.get(i).getAnnotations())
                annotation.visitMethodParam(i, methodVisitor);
        }

        if (isAbstract()) {
            methodVisitor.visitEnd();
            visitor.visitEnd();
            return;
        }

        methodVisitor.visitCode();

        // Accepting the instructions writer
        if (getWriter() != null) getWriter().accept(this, methodVisitor);

        methodVisitor.visitInsn(getReturnType().getType().getOpcode(Opcodes.IRETURN));
        methodVisitor.visitMaxs(0, 0); // should be automatically calculated by ClassBuilder's ClassVisitor

        methodVisitor.visitEnd();
        visitor.visitEnd();
    }

    /**
     * @return return type of the method
     */
    public Parameter getReturnType() {
        return returnType;
    }

    /**
     * @return parameters of the method
     */
    public @Unmodifiable List<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Adds new parameter to the method.
     *
     * @param parameter parameter to add
     */
    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    /**
     * Adds new parameter to the method at given index.
     *
     * @param index index of the parameter
     * @param parameter parameter to add
     */
    public void addParameter(int index, Parameter parameter) {
        parameters.add(index, parameter);
    }

    /**
     * Changes method parameters.
     *
     * @param parameters new parameters
     */
    public void setParameters(Collection<Parameter> parameters) {
        this.parameters.clear();
        this.parameters.addAll(parameters);
    }

    /**
     * Clears all parameters of this method.
     */
    public void clearParameters() {
        parameters.clear();
    }

    /**
     * @return exceptions that can be thrown by this method
     */
    public @Unmodifiable Collection<Type> getExceptions() {
        return Collections.unmodifiableCollection(exceptions);
    }

    /**
     * @return bytecode writer for this method
     */
    public @Nullable BiConsumer<Method, MethodVisitor> getWriter() {
        return writer;
    }

    /**
     * @param writer new bytecode writer for this method
     */
    public void setWriter(@Nullable BiConsumer<Method, MethodVisitor> writer) {
        this.writer = writer;
    }

    /**
     * @return sum of all parameters of this method
     */
    public int getSize() {
        int size = 0;
        if (!isStatic()) size++;
        for (Parameter parameter : parameters)
            size += parameter.getType().getSize();
        return size;
    }

}
