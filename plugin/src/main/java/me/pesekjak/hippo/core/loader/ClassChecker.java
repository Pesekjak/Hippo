package me.pesekjak.hippo.core.loader;

import me.pesekjak.hippo.core.AbstractClass;
import me.pesekjak.hippo.core.skript.Storage;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Util to check whether the hierarchy of the class is legit and safe to compile.
 */
public class ClassChecker {

    private final Set<Type> successful = new LinkedHashSet<>();
    private final Map<Type, CompileException> failed = new LinkedHashMap<>();

    /**
     * @return types of classes that pass the checks
     */
    public Set<Type> getSuccessful() {
        return successful;
    }

    /**
     * Adds types to the successful classes.
     * <p>
     * Used for speeding checks when we know that some classes are always safe
     * because they have been already compiled.
     *
     * @param successful safe to compile classes
     */
    public void addSuccessful(Collection<Type> successful) {
        this.successful.addAll(successful);
    }

    /**
     * @return map of classes that failed the checks and reasons why
     */
    public Map<Type, CompileException> getFailed() {
        return failed;
    }

    /**
     * Adds new checked classes to the successful classes.
     * <p>
     * The order of the checked classes needs to be reversed order of the
     * class hierarchy (child first, parent last) where the type parameter
     * is the last child in the hierarchy.
     *
     * @param type last child in the hierarchy
     * @param checked class hierarchy
     * @return true
     */
    private boolean success(Type type, List<Type> checked) {
        ArrayList<Type> sorted = new ArrayList<>(checked);
        Collections.reverse(sorted);
        sorted.add(type);

        successful.addAll(sorted);
        return true;
    }

    /**
     * Adds new classes to the classes that failed the checks.
     * <p>
     * The order of the checked classes needs to be reversed order of the
     * class hierarchy (child first, parent last) where the type parameter
     * is the last child in the hierarchy.
     *
     * @param type last child in the hierarchy
     * @param checked class hierarchy
     * @param reason reason why the classes can not be compiled
     * @return false
     */
    private boolean fail(Type type, List<Type> checked, CompileException reason) {
        ArrayList<Type> sorted = new ArrayList<>(checked);
        Collections.reverse(sorted);
        sorted.add(type);

        sorted.forEach(t -> failed.put(t, reason));
        sorted.forEach(successful::remove);
        return false;
    }

    /**
     * Checks whether the given class has legit super class.
     *
     * @param source source class
     * @return whether the class has legit super class
     */
    public boolean hasLegitSuperClass(AbstractClass source) {
        return isLegitSuperClass(source.getType(), source.getSuperClass(), Collections.singletonList(source.getType()));
    }

    /**
     * Checks whether the given class has legit super class.
     *
     * @param owner type of the class
     * @param type its super class type
     * @param checked already checked types
     * @return whether the class has legit super class
     */
    private boolean isLegitSuperClass(Type owner, Type type, List<Type> checked) {
        if (successful.contains(type)) return success(type, checked);
        if (failed.containsKey(type)) return fail(type, checked, failed.get(type));

        if (checked.contains(type)) return fail(type, checked, new CompileException(CompileException.CYCLIC_INHERITANCE, owner));

        DynamicClassLoader loader = DynamicClassLoader.getInstance();
        String className = type.getClassName();

        try {
            Class<?> found = loader.loadClass(className);
            int modifier = found.getModifiers();

            if (Modifier.isFinal(modifier)) return fail(type, checked, new CompileException(CompileException.EXTENDS_FINAL, owner));
            if (Modifier.isInterface(modifier)) return fail(type, checked, new CompileException(CompileException.EXTENDS_INTERFACE, owner));

            return success(type, checked);
        } catch (ClassNotFoundException ignored) { }

        if (!Storage.contains(className)) return fail(type, checked, new CompileException(CompileException.EXTENDS_NON_EXISTING, owner));
        AbstractClass clazz = Storage.of(className).getSourceClass().getWrappedClass();

        if (clazz.isFinal()) return fail(type, checked, new CompileException(CompileException.EXTENDS_FINAL, owner));
        if (clazz.isInterface()) return fail(type, checked, new CompileException(CompileException.EXTENDS_INTERFACE, owner));

        List<Type> nextChecked = new ArrayList<>(checked);
        nextChecked.add(type);

        return isLegitSuperClass(clazz.getType(), clazz.getSuperClass(), nextChecked);
    }

    /**
     * Checks whether the given class has legit interfaces.
     *
     * @param source source class
     * @return whether the class has legit interfaces
     */
    public boolean hasLegitInterfaces(AbstractClass source) {
        for (Type nextInterface : source.getInterfaces()) {
            if (isLegitInterface(source.getType(), nextInterface, Collections.singletonList(source.getType()))) continue;
            return false;
        }
        return true;
    }

    /**
     * Checks whether the given class has legit interfaces.
     *
     * @param owner type of the class
     * @param type its interface type
     * @param checked already checked types
     * @return whether the class has legit interfaces
     */
    private boolean isLegitInterface(Type owner, Type type, List<Type> checked) {
        if (successful.contains(type)) return success(type, checked);
        if (failed.containsKey(type)) return fail(type, checked, failed.get(type));

        if (checked.contains(type)) return fail(type, checked, new CompileException(CompileException.CYCLIC_INHERITANCE, owner));

        DynamicClassLoader loader = DynamicClassLoader.getInstance();
        String className = type.getClassName();

        try {
            Class<?> found = loader.loadClass(className);
            int modifier = found.getModifiers();

            if (!Modifier.isInterface(modifier))
                return fail(type, checked, new CompileException(CompileException.IMPLEMENTS_NON_INTERFACE, owner));

            return true;
        } catch (ClassNotFoundException ignored) { }

        if (!Storage.contains(className)) return fail(type, checked, new CompileException(CompileException.IMPLEMENTS_NON_EXISTING, owner));
        AbstractClass clazz = Storage.of(className).getSourceClass().getWrappedClass();

        if (!clazz.isInterface()) return fail(type, checked, new CompileException(CompileException.IMPLEMENTS_NON_INTERFACE, owner));

        List<Type> nextChecked = new ArrayList<>(checked);
        nextChecked.add(type);

        for (Type nextInterface : clazz.getInterfaces()) {
            if (isLegitInterface(clazz.getType(), nextInterface, nextChecked)) continue;
            return false;
        }

        return true;
    }

}
