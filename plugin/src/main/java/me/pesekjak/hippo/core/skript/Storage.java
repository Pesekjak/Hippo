package me.pesekjak.hippo.core.skript;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.Variable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import me.pesekjak.hippo.bukkit.*;
import me.pesekjak.hippo.core.Constants;
import me.pesekjak.hippo.core.NamedParameter;
import me.pesekjak.hippo.elements.effects.EffEnum;
import me.pesekjak.hippo.elements.effects.EffSuperConstructorCall;
import me.pesekjak.hippo.utils.ExceptionThrower;
import me.pesekjak.hippo.utils.SkriptUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage of all currently existing custom classes and their
 * content.
 */
public final class Storage {

    private static final Map<String, Storage> STORAGE = new ConcurrentHashMap<>();

    private final ClassWrapper clazz;
    private final Table<String, String, ClassContentSkriptWrapper> classStorage;

    /**
     * Returns storage for a class with given name.
     *
     * @param className name of the class
     * @return Storage for class with given name
     * @throws NullPointerException if there is no class with given name
     */
    public static Storage of(String className) {
        Storage storage = STORAGE.get(className);
        if (storage == null) throw new NullPointerException("There is no class with name '" + className + "'");
        return storage;
    }

    /**
     * Returns storage for given class.
     *
     * @param clazz class
     * @return Storage for given class
     * @throws NullPointerException if given class has no storage
     */
    public static Storage of(ClassWrapper clazz) {
        return of(clazz.getWrappedClass().getName());
    }

    /**
     * Creates new empty storage for given class.
     *
     * @param clazz class to create the storage for
     */
    public static void create(ClassWrapper clazz) {
        if (contains(clazz)) throw new IllegalStateException();
        STORAGE.put(clazz.getWrappedClass().getName(), new Storage(clazz));
    }

    /**
     * @param className name of the class
     * @return whether there is a storage for class with given name
     */
    public static boolean contains(String className) {
        return STORAGE.containsKey(className);
    }

    /**
     * @param clazz class
     * @return whether there is a storage for given class
     */
    public static boolean contains(ClassWrapper clazz) {
        return contains(clazz.getWrappedClass().getName());
    }

    /**
     * Clears storage for class with given name.
     *
     * @param className name of the class
     */
    public static void clear(String className) {
        STORAGE.remove(className);
    }

    /**
     * Clears storage for given class.
     *
     * @param clazz class
     */
    public static void clear(ClassWrapper clazz) {
        clear(clazz.getWrappedClass().getName());
    }

    /**
     * @return all current class storages
     */
    public static @Unmodifiable Collection<Storage> getAll() {
        return Collections.unmodifiableCollection(STORAGE.values());
    }

    @SuppressWarnings("UnstableApiUsage")
    private Storage(ClassWrapper clazz) {
        this.clazz = Objects.requireNonNull(clazz);
        classStorage = Tables.newCustomTable(new ConcurrentHashMap<>(), ConcurrentHashMap::new);
    }

    /**
     * @return source class for the storage
     */
    public ClassWrapper getSourceClass() {
        return clazz;
    }

    /**
     * Returns inner class with given name and descriptor.
     *
     * @param name name of the class
     * @param descriptor descriptor of the class
     * @return inner class or null if there is none
     */
    public @Nullable ClassWrapper getInnerClass(String name, String descriptor) {
        ClassContentSkriptWrapper wrapper = classStorage.get(name, descriptor);
        return wrapper instanceof ClassWrapper classWrapper ? classWrapper : null;
    }

    /**
     * Returns constructor with given descriptor.
     *
     * @param descriptor descriptor of the constructor
     * @return constructor or null if there is none
     */
    public @Nullable ConstructorWrapper getConstructor(String descriptor) {
        ClassContentSkriptWrapper wrapper = classStorage.get(Constants.CONSTRUCTOR_METHOD_NAME, descriptor);
        return wrapper instanceof ConstructorWrapper constructorWrapper ? constructorWrapper : null;
    }

    /**
     * Returns field with given name.
     *
     * @param name name of the field
     * @return field or null if there is none
     */
    public @Nullable FieldWrapper getField(String name) {
        Collection<ClassContentSkriptWrapper> column = classStorage.row(name).values();
        if (column.size() == 0) return null;
        ClassContentSkriptWrapper next = column.iterator().next();
        return next instanceof FieldWrapper fieldWrapper ? fieldWrapper : null;
    }

    /**
     * Returns enum with given name.
     *
     * @param name name of the enum
     * @return enum or null if there is none
     */
    public @Nullable EnumWrapper getEnum(String name) {
        Collection<ClassContentSkriptWrapper> column = classStorage.row(name).values();
        if (column.size() == 0) return null;
        ClassContentSkriptWrapper next = column.iterator().next();
        return next instanceof EnumWrapper enumWrapper ? enumWrapper : null;
    }

    /**
     * Returns method with given name and descriptor.
     *
     * @param name name of the method
     * @param descriptor descriptor of the method
     * @return method or null if there is none
     */
    public @Nullable MethodWrapper getMethod(String name, String descriptor) {
        ClassContentSkriptWrapper wrapper = classStorage.get(name, descriptor);
        return wrapper instanceof MethodWrapper methodWrapper ? methodWrapper : null;
    }

    /**
     * Returns static block of the class.
     *
     * @return static block
     */
    public @Nullable StaticBlockWrapper getStaticBlock() {
        ClassContentSkriptWrapper wrapper = classStorage.get(Constants.STATIC_BLOCK_METHOD_NAME, Constants.STATIC_BLOCK_METHOD_DESCRIPTOR);
        return wrapper instanceof StaticBlockWrapper staticBlockWrapper ? staticBlockWrapper : null;
    }

    /**
     * @return table of the storage
     */
    public Table<String, String, ClassContentSkriptWrapper> getTable() {
        return classStorage;
    }

    /**
     * Returns value of a field for a given class and its instance.
     * <p>
     * Is used in the compiled classes to get initial field values.
     *
     * @param className name of the class
     * @param name name of the field
     * @param instance instance, can be null for static fields
     * @return initial field value
     */
    public static @Nullable Object getFieldValue(String className, String name, @Nullable Object instance) {
        Storage storage = of(className);
        FieldWrapper fieldWrapper = storage.getField(name);
        if (fieldWrapper == null) return null;
        Expression<?> expression = fieldWrapper.value();
        if (expression == null) return null;
        return expression.getSingle(new FieldCallEvent(storage.getSourceClass().getWrappedClass(), instance));
    }

    /**
     * Returns values of enum constant for a given class for its constructor.
     * <p>
     * Is used in the compiled classes to initiate enum constants.
     *
     * @param className name of the class
     * @param name name of the field
     * @param expectedSize number of arguments to return
     * @return arguments to use to construct the enum
     */
    public static @Nullable Object[] getEnumValues(String className, String name, int expectedSize) {
        Object[] toReturn = new Object[expectedSize];
        Storage storage = of(className);
        EnumWrapper enumWrapper = storage.getEnum(name);
        if (enumWrapper == null) return toReturn;
        EffEnum enumEffect = enumWrapper.enumEffect();
        if (enumEffect == null) return toReturn;

        EnumValuesEvent event = new EnumValuesEvent();

        enumEffect.execute(event);

        for (int i = 0; i < Math.max(toReturn.length, event.getArguments().size()); i++)
            toReturn[i] = event.getArguments().get(i);

        return toReturn;
    }

    /**
     * Invokes a method for a given class and its instance.
     * <p>
     * Is used in the compiled classes to invoke methods.
     *
     * @param className name of the class
     * @param name name of the method
     * @param descriptor descriptor of the method
     * @param instance instance, can be null for static methods
     * @param arguments arguments to invoke the method with
     * @return returned value by the method
     */
    public static @Nullable Object runMethod(String className, String name, String descriptor, @Nullable Object instance, Object[] arguments) {
        Storage storage = of(className);
        MethodWrapper methodWrapper = storage.getMethod(name, descriptor);
        if (methodWrapper == null) return null;
        Trigger trigger = methodWrapper.trigger();
        if (trigger == null) return null;

        MethodCallEvent event = new MethodCallEvent(storage.getSourceClass().getWrappedClass(), instance);

        for (int i = 0; i < Math.max(methodWrapper.arguments().size(), arguments.length); i++) {
            NamedParameter namedParameter = methodWrapper.arguments().get(i);
            Variable<?> variable = Variable.newInstance("_" + namedParameter.name(), new Class<?>[]{Object.class});
            if (variable == null) continue;
            SkriptUtil.setVariable(variable, event, arguments[i]);
        }

        trigger.execute(event);

        if (event.getThrowable() != null)
            ExceptionThrower.throwException(event.getThrowable());

        return event.getReturned();
    }

    /**
     * Gets values used to call super constructors.
     * <p>
     * Is used in the compiled classes to construct the object.
     *
     * @param className name of the class
     * @param descriptor descriptor of the method
     * @param expectedSize number of arguments to return
     * @param arguments arguments used to invoke the constructor
     * @return arguments used in super constructor call
     */
    public static @Nullable Object[] runConstructorSuper(String className, String descriptor, int expectedSize, Object[] arguments) {
        Object[] toReturn = new Object[expectedSize];
        Storage storage = of(className);
        ConstructorWrapper constructorWrapper = storage.getConstructor(descriptor);
        if (constructorWrapper == null) return toReturn;
        EffSuperConstructorCall superCall = constructorWrapper.superCall();
        if (superCall == null) return toReturn;

        ConstructorSuperCallEvent event = new ConstructorSuperCallEvent();

        for (int i = 0; i < Math.max(constructorWrapper.arguments().size(), arguments.length); i++) {
            NamedParameter namedParameter = constructorWrapper.arguments().get(i);
            Variable<?> variable = Variable.newInstance("_" + namedParameter.name(), new Class<?>[]{Object.class});
            if (variable == null) continue;
            SkriptUtil.setVariable(variable, event, arguments[i]);
        }

        superCall.execute(event);

        for (int i = 0; i < Math.max(toReturn.length, event.getArguments().size()); i++)
            toReturn[i] = event.getArguments().get(i);

        return toReturn;
    }

    /**
     * Invokes a constructor for a given class and its instance.
     * <p>
     * Is used in the compiled classes to invoke constructors.
     *
     * @param className name of the class
     * @param descriptor descriptor of the constructor
     * @param instance instance
     * @param arguments arguments to invoke the constructor with
     */
    public static void runConstructor(String className, String descriptor, Object instance, Object[] arguments) {
        Storage storage = of(className);
        ConstructorWrapper constructorWrapper = storage.getConstructor(descriptor);
        if (constructorWrapper == null) return;
        Trigger trigger = constructorWrapper.trigger();
        if (trigger == null) return;

        ConstructorCallEvent event = new ConstructorCallEvent(storage.getSourceClass().getWrappedClass(), instance);

        for (int i = 0; i < Math.max(constructorWrapper.arguments().size(), arguments.length); i++) {
            NamedParameter namedParameter = constructorWrapper.arguments().get(i);
            Variable<?> variable = Variable.newInstance("_" + namedParameter.name(), new Class<?>[]{Object.class});
            if (variable == null) continue;
            SkriptUtil.setVariable(variable, event, arguments[i]);
        }

        trigger.execute(event);

        if (event.getThrowable() != null)
            ExceptionThrower.throwException(event.getThrowable());
    }

    /**
     * Invokes static block of a class.
     * <p>
     * Is used in the compiled classes to invoke static blocks.
     *
     * @param className name of the class
     */
    public static void runStaticBlock(String className) {
        Storage storage = of(className);
        StaticBlockWrapper staticBlockWrapper = storage.getStaticBlock();
        if (staticBlockWrapper == null) return;

        StaticBlockCallEvent event = new StaticBlockCallEvent(storage.getSourceClass().getWrappedClass());

        for (Trigger trigger : staticBlockWrapper.staticTriggers()) {
            trigger.execute(event);
            if (event.getThrowable() != null) ExceptionThrower.throwException(event.getThrowable());
        }
    }

}
