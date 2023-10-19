package me.pesekjak.hippo.core.annotations;

import me.pesekjak.hippo.core.Visitable;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a Java annotation.
 */
public class Annotation implements Visitable, AnnotationVisitable {

    private final Type type;
    private final Map<String, AnnotationVisitable> values = new HashMap<>();

    public Annotation(Type type, Map<String, AnnotationVisitable> values) {
        this.type = Objects.requireNonNull(type);
        this.values.putAll(values);
    }

    public Annotation(Type type) {
        this(type, Collections.emptyMap());
    }

    public Annotation(String typeDescriptor, Map<String, AnnotationVisitable> values) {
        this(Type.getType(typeDescriptor), values);
    }

    public Annotation(String typeDescriptor) {
        this(typeDescriptor, Collections.emptyMap());
    }

    @Override
    public void visit(ClassVisitor visitor) {
        visitAndEnd(() -> visitor.visitAnnotation(getType().getDescriptor(), true));
    }

    @Override
    public void visit(@Nullable String name, AnnotationVisitor visitor) {
        visitAndEnd(() -> visitor.visitAnnotation(name, getType().getDescriptor()));
    }

    /**
     * Visits the annotation using field visitor.
     *
     * @param visitor visitor
     */
    public void visit(FieldVisitor visitor) {
        visitAndEnd(() -> visitor.visitAnnotation(getType().getDescriptor(), true));
    }

    /**
     * Visits the annotation using method visitor.
     *
     * @param visitor visitor
     */
    public void visit(MethodVisitor visitor) {
        visitAndEnd(() -> visitor.visitAnnotation(getType().getDescriptor(), true));
    }

    /**
     * Visits the annotation using method visitor on a method parameter.
     *
     * @param index index of the parameter
     * @param visitor visitor
     */
    public void visitMethodParam(int index, MethodVisitor visitor) {
        visitAndEnd(() -> visitor.visitParameterAnnotation(index, getType().getDescriptor(), true));
    }

    /**
     * Visits the annotation using field visitor on given type.
     *
     * @param typeRef reference of the type
     * @param typePath path to the type
     * @param visitor visitor
     */
    public void visitType(int typeRef, TypePath typePath, FieldVisitor visitor) {
        visitAndEnd(() -> visitor.visitTypeAnnotation(typeRef, typePath, getType().getDescriptor(), true));
    }

    /**
     * Visits the annotation using method visitor on given type.
     *
     * @param typeRef reference of the type
     * @param typePath path to the type
     * @param visitor visitor
     */
    public void visitType(int typeRef, TypePath typePath, MethodVisitor visitor) {
        visitAndEnd(() -> visitor.visitTypeAnnotation(typeRef, typePath, getType().getDescriptor(), true));
    }

    /**
     * Visits end on the annotation visitor provided by the supplier.
     *
     * @param supplier visitor
     */
    private void visitAndEnd(Supplier<AnnotationVisitor> supplier) {
        AnnotationVisitor annotationVisitor = supplier.get();
        getValues().forEach((name, value) -> value.visit(name, annotationVisitor));
        annotationVisitor.visitEnd();
    }

    /**
     * @return type of the annotation
     */
    public Type getType() {
        return type;
    }

    /**
     * @return map of annotation parameters
     */
    public @Unmodifiable Map<String, AnnotationVisitable> getValues() {
        return Collections.unmodifiableMap(values);
    }

}
