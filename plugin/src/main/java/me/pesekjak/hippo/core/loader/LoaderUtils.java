package me.pesekjak.hippo.core.loader;

import me.pesekjak.hippo.core.AbstractClass;
import me.pesekjak.hippo.core.skript.ClassWrapper;
import me.pesekjak.hippo.core.skript.Storage;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utils for getting children and parents of classes in the hierarchy.
 */
public final class LoaderUtils {

    private LoaderUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns all dependent classes of this class. (Class that are lower in the hierarchy)
     * @param clazz class
     * @return its dependant classes
     */
    public static @Unmodifiable Collection<AbstractClass> getDependant(AbstractClass clazz) {
        Set<AbstractClass> dependant = new HashSet<>();
        Set<AbstractClass> classes = Storage.getAll().stream()
                .map(Storage::getSourceClass)
                .map(ClassWrapper::getWrappedClass)
                .collect(Collectors.toSet());
        classes.removeIf(c -> getDependencies(c).isEmpty());

        dependant.add(clazz);
        int previousCycle = 0;
        int nextCycle = dependant.size(); // 1

        while (previousCycle != nextCycle) {
            previousCycle = nextCycle;
            Set<AbstractClass> toRemove = new HashSet<>();
            for (AbstractClass next : classes) {
                if (getDependencies(next).stream().noneMatch(dependant::contains)) continue;
                toRemove.add(next);
                dependant.add(next);
            }
            nextCycle = dependant.size();
            classes.removeAll(toRemove);
        }

        return Collections.unmodifiableSet(dependant);
    }

    /**
     * Returns dependencies by the provided class.
     * <p>
     * Only its super class and declared interfaces.
     *
     * @param clazz class
     * @return its dependencies
     */
    public static @Unmodifiable Collection<AbstractClass> getDependencies(AbstractClass clazz) {
        Set<AbstractClass> dependencies = new HashSet<>();

        Type superType = clazz.getSuperClass();
        if (Storage.contains(superType.getClassName()))
            dependencies.add(Storage.of(superType.getClassName()).getSourceClass().getWrappedClass());

        for (Type interfaceType : clazz.getInterfaces()) {
            if (!Storage.contains(interfaceType.getClassName())) continue;
            dependencies.add(Storage.of(interfaceType.getClassName()).getSourceClass().getWrappedClass());
        }

        return Collections.unmodifiableSet(dependencies);
    }

}
