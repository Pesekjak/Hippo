package me.pesekjak.hippo.core;

import me.pesekjak.hippo.core.annotations.Annotation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

/**
 * Represents a Java class that can be visited with ASM class visitor.
 */
public abstract non-sealed class AbstractClass implements Modifiable, ClassContent {

    private final @Nullable AbstractClass outerClass;
    private final Type type;
    private final Type superClass;
    private final Collection<Type> interfaces;
    private final int modifier;
    private final Collection<Annotation> annotations;

    private final List<Field> fields = new LinkedList<>();
    private final List<Constructor> constructors = new LinkedList<>();
    private final List<Method> methods = new LinkedList<>();
    private final StaticBlock staticBlock = new StaticBlock(this);
    private final List<AbstractClass> innerClasses = new LinkedList<>();

    private int version = Opcodes.V17;

    protected AbstractClass(@Nullable AbstractClass outerClass,
                            Type type,
                            @Nullable Type superClass,
                            Collection<Type> interfaces,
                            int modifier,
                            Collection<Annotation> annotations) throws IllegalModifiersException {
        this.outerClass = outerClass;
        this.type = Objects.requireNonNull(type);
        this.superClass = superClass != null ? superClass : Type.getType(Object.class);
        this.interfaces = Objects.requireNonNull(interfaces);
        this.modifier = modifier;
        this.annotations = Objects.requireNonNull(annotations);
        Modifiable.checkModifiers(this, "Class '" + getName() + "'");
        if (outerClass == null && !isPublic()) throw new IllegalModifiersException("Class '" + getName() + "' needs to be public");
    }

    @Override
    public int getModifier() {
        return modifier;
    }

    @Override
    public int getCompatibleModifiers() {
        return Modifiers.getClassModifiers();
    }

    @Override
    public void visit(ClassVisitor visitor) {
        String[] interfaces = getInterfaces().stream().map(Type::getInternalName).toArray(String[]::new);
        visitor.visit(version, getModifier(), getType().getInternalName(), null, getSuperClass().getInternalName(), interfaces);
        getAnnotations().forEach(annotation -> annotation.visit(visitor));
        getContents().forEach(content -> content.visit(visitor));
        visitor.visitEnd();
        getInnerClasses().forEach(innerClass -> innerClass.visit(visitor));
    }


    @Override
    public @Unmodifiable Collection<Annotation> getAnnotations() {
        return Collections.unmodifiableCollection(annotations);
    }

    @Override
    public @Nullable AbstractClass getSource() {
        return outerClass;
    }

    @Override
    public String getName() {
        return getType().getClassName();
    }

    @Override
    public String getDescriptor() {
        return getType().getDescriptor();
    }

    /**
     * @return type of the class
     */
    public Type getType() {
        return type;
    }

    /**
     * @return type of the super class
     */
    public Type getSuperClass() {
        return superClass;
    }

    /**
     * @return interfaces of the class
     */
    public @Unmodifiable Collection<Type> getInterfaces() {
        return Collections.unmodifiableCollection(interfaces);
    }

    /**
     * @return fields of the class
     */
    public @Unmodifiable List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * Returns field with given name or null if there is none.
     *
     * @param name name of the field
     * @return field
     */
    public @Nullable Field getField(String name) {
        return fields.stream().filter(f -> f.getName().equals(name)).findAny().orElse(null);
    }

    /**
     * Adds a field to this class.
     *
     * @param field field to add
     * @return whether the field has been added
     * @throws IllegalClassContentException if the provided field is illegal for this class
     */
    public boolean addField(Field field) throws IllegalClassContentException {
        checkClassContent(field);
        if (fields.stream().anyMatch(f -> f.getName().equals(field.getName())))
            throw new IllegalArgumentException("Field with name '" + field.getName() + "' is already part of the class");
        return fields.add(field);
    }

    /**
     * Removes a field from this class.
     *
     * @param field field to remove
     * @return whether the field has been removed
     */
    public boolean removeField(Field field) {
        return fields.remove(field);
    }

    /**
     * @return constructors of this class
     */
    public @Unmodifiable List<Constructor> getConstructors() {
        return Collections.unmodifiableList(constructors);
    }

    /**
     * Adds a constructor to this class.
     *
     * @param constructor constructor to add
     * @return whether the constructor has been added
     * @throws IllegalClassContentException if the provided constructor is illegal for this class
     */
    public boolean addConstructor(Constructor constructor) throws IllegalClassContentException {
        checkClassContent(constructor);
        if (constructors.stream().anyMatch(c -> c.getDescriptor().equals(constructor.getDescriptor())))
            throw new IllegalArgumentException("Constructor with descriptor '" + constructor.getDescriptor() + "' is already part of the class");
        return constructors.add(constructor);
    }

    /**
     * Removes a constructor from this class.
     *
     * @param constructor constructor to remove
     * @return whether the constructor has been removed
     */
    public boolean removeConstructor(Constructor constructor) {
        return constructors.remove(constructor);
    }

    /**
     * @return methods of this class
     */
    public @Unmodifiable List<Method> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    /**
     * Adds a method to this class.
     *
     * @param method method to add
     * @return whether the method has been added
     * @throws IllegalClassContentException if the provided method is illegal for this class
     */
    public boolean addMethod(Method method) throws IllegalClassContentException {
        checkClassContent(method);
        if (methods.stream().anyMatch(m -> m.getName().equals(method.getName()) && m.getDescriptor().equals(method.getDescriptor())))
            throw new IllegalArgumentException("Method with name '" + method.getName() + "' and descriptor '" + method.getDescriptor() + "'"
                    + " is already part of the class");
        return methods.add(method);
    }

    /**
     * Removes a method from this class.
     *
     * @param method method to remove
     * @return whether the method has been removed
     */
    public boolean removeMethod(Method method) {
        return methods.remove(method);
    }

    /**
     * @return inner classes of this class
     */
    public @Unmodifiable List<AbstractClass> getInnerClasses() {
        return Collections.unmodifiableList(innerClasses);
    }

    /**
     * Adds inner class to this class.
     *
     * @param innerClass inner class to add
     * @return whether the inner class has been added
     * @throws IllegalClassContentException if the provided inner class is illegal for this class
     */
    public boolean addInnerClass(AbstractClass innerClass) throws IllegalClassContentException {
        if (innerClasses.stream().anyMatch(c -> c.getType().getClassName().equals(innerClass.getType().getClassName())))
            throw new IllegalArgumentException("Inner class '" + innerClass.getType().getClassName() + "' is already part of the class");
        return innerClasses.add(innerClass);
    }

    /**
     * Removes inner class from this class.
     *
     * @param innerClass inner class to remove
     * @return whether the inner class has been removed
     */
    public boolean removeInnerClass(AbstractClass innerClass) {
        return innerClasses.remove(innerClass);
    }

    /**
     * @return static block of this class
     */
    public StaticBlock getStaticBlock() {
        return staticBlock;
    }

    /**
     * @return all contents of this class
     */
    public Collection<ClassContent> getContents() {
        List<ClassContent> contents = new ArrayList<>();
        contents.addAll(getFields());
        contents.addAll(getConstructors());
        contents.addAll(getMethods());
        if (getStaticBlock() != null) contents.add(getStaticBlock());
        return contents;
    }

    /**
     * Checks whether the class content can be added to this class.
     *
     * @param content content to check
     * @throws IllegalClassContentException if the content is illegal for this class
     */
    private void checkClassContent(ClassContent content) throws IllegalClassContentException {
        if (content instanceof StaticBlock || content instanceof AbstractClass) return;

        if (content instanceof Field field) checkField(field);
        else if (content instanceof Constructor constructor) checkConstructor(constructor);
        else if (content instanceof Method method) checkMethod(method);
    }

    /**
     * Checks whether a field can be added to this class.
     *
     * @param field field to check
     * @throws IllegalClassContentException if the field is illegal for this class
     */
    protected abstract void checkField(Field field) throws IllegalClassContentException;

    /**
     * Checks whether a constructor can be added to this class.
     *
     * @param constructor constructor to check
     * @throws IllegalClassContentException if the constructor is illegal for this class
     */
    protected abstract void checkConstructor(Constructor constructor) throws IllegalClassContentException;

    /**
     * Checks whether a method can be added to this class.
     *
     * @param method method to check
     * @throws IllegalClassContentException if the method is illegal for this class
     */
    protected abstract void checkMethod(Method method) throws IllegalClassContentException;

    /**
     * Called when constructor of outer class is visited.
     *
     * @param constructor constructor being visited
     * @param visitor visitor
     */
    public abstract void visitOuterInit(Constructor constructor, MethodVisitor visitor);

    /**
     * Called when static block of outer class is visited.
     *
     * @param method static block being visited
     * @param visitor visitor
     */
    public abstract void visitOuterClinit(Method method, MethodVisitor visitor);

    /**
     * @return Java ClassFile version for this class
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version new Java ClassFile version for this class
     */
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return getName();
    }

}
