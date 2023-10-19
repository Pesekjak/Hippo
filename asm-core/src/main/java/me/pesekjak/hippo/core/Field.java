package me.pesekjak.hippo.core;

import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Represents a class field.
 */
public final class Field implements ClassContent {

    private final AbstractClass source;
    private final String name;
    private final Parameter type;
    private final int modifier;
    private final Collection<Annotation> annotations;

    private @Nullable BiConsumer<Method, MethodVisitor> initializer;

    public Field(AbstractClass source,
                 String name,
                 Parameter type,
                 int modifier,
                 Collection<Annotation> annotations) throws IllegalModifiersException {
        this.source = Objects.requireNonNull(source);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.modifier = modifier;
        this.annotations = new ArrayList<>(Objects.requireNonNull(annotations));
        Modifiable.checkModifiers(this, "Field '" + getName() + "'");
        if (type.getType().getDescriptor().endsWith("V")) throw new IllegalStateException();
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
        return getType().getType().getDescriptor();
    }

    @Override
    public void visitInit(Constructor constructor, MethodVisitor visitor) {
        if (isStatic()) return;
        if (getInitializer() == null) return;
        getInitializer().accept(constructor, visitor);
    }

    @Override
    public void visitClinit(Method method, MethodVisitor visitor) {
        if (!isStatic()) return;
        if (getInitializer() == null) return;
        getInitializer().accept(method, visitor);
    }

    @Override
    public int getModifier() {
        return modifier;
    }

    @Override
    public int getCompatibleModifiers() {
        return Modifiers.getFieldModifiers();
    }

    @Override
    public void visit(ClassVisitor visitor) {
        FieldVisitor fieldVisitor = visitor.visitField(getModifier(), getName(), getDescriptor(), null, null);

        // Visiting field and type annotations
        getAnnotations().forEach(annotation -> annotation.visit(fieldVisitor));
        getType().getAnnotations().forEach(annotation -> annotation.visitType(TypeReference.FIELD, null, fieldVisitor));

        fieldVisitor.visitEnd();
        visitor.visitEnd();
    }

    /**
     * @return type of the field
     */
    public Parameter getType() {
        return type;
    }

    /**
     * @return bytecode writer for this field
     */
    public @Nullable BiConsumer<Method, MethodVisitor> getInitializer() {
        return initializer;
    }

    /**
     * @param initializer new bytecode writer for this field
     */
    public void setInitializer(@Nullable BiConsumer<Method, MethodVisitor> initializer) {
        this.initializer = initializer;
    }

}
