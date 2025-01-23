package me.pesekjak.hippo.core.loader;

import com.btk5h.skriptmirror.LibraryLoader;
import me.pesekjak.hippo.core.AbstractClass;
import me.pesekjak.hippo.core.skript.ClassWrapper;
import me.pesekjak.hippo.core.skript.Storage;
import me.pesekjak.hippo.utils.SkriptUtil;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Class loader that can dynamically load and unload classes using {@link SingleClassLoader}.
 * <p>
 * Only a single instance of this class loader exists at the time {@link DynamicClassLoader#getInstance()}.
 */
public class DynamicClassLoader extends ClassLoader {

    private static ClassLoader emptyClassLoader;
    private static DynamicClassLoader instance;

    final Map<String, SingleClassLoader> loaders = new ConcurrentHashMap<>();
    final Map<String, CompileException> compileExceptions = new ConcurrentHashMap<>();

    /**
     * Injects class loader used by reflect.
     * <p>
     * This needs to be called during Hippo initialization.
     */
    public static void injectReflectClassLoader() {
        try {

            emptyClassLoader = LibraryLoader.getClassLoader();

            instance = new DynamicClassLoader(emptyClassLoader);
            Field classLoaderField = LibraryLoader.class.getDeclaredField("classLoader");
            classLoaderField.setAccessible(true);
            classLoaderField.set(null, instance);

        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * @return instance of the class loader
     */
    public static DynamicClassLoader getInstance() {
        return instance;
    }

    DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        SingleClassLoader loader = loaders.get(name);
        // finding custom classes
        if (loader != null) return loader.loadClass(name);
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException exception) {
            // classes provided by reflect
            return emptyClassLoader.loadClass(name);
        }
    }

    /**
     * Pushes changes from a result of a class update.
     *
     * @param result result to push
     */
    public void pushUpdate(ClassUpdate.Result result) {
        removeOld();
        loadSuccessful(result.successful());
        loadFailed(
                result.failed().entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey().getClassName(), entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                true
        );
    }

    /**
     * Returns compile error for a class with given name.
     *
     * @param className name of the class
     * @return compile error for the class or null if there is none
     */
    public @Nullable String getCompileError(String className) {
        return Optional.ofNullable(compileExceptions.get(className)).map(Exception::getMessage).orElse(null);
    }

    /**
     * Removes class with given name from the class loader.
     *
     * @param className name of the class to remove
     */
    private void remove(String className) {
        loaders.remove(className);
        compileExceptions.remove(className);
    }

    /**
     * Removes classes that are no longer referenced in the {@link Storage}. (Classes that have been removed by the user)
     */
    private void removeOld() {
        List<AbstractClass> old = loaders.values().stream()
                .map(SingleClassLoader::getClassSignature)
                .map(ClassSignature::clazz)
                .toList();
        for (AbstractClass clazz : old) {
            if (Storage.contains(clazz.getName())) continue;
            remove(clazz.getName());
        }
    }

    /**
     * Loads new classes from their signatures.
     *
     * @param signatures signatures to load
     */
    private void loadSuccessful(Collection<ClassSignature> signatures) {
        for (ClassSignature signature : signatures) {
            remove(signature.clazz().getName());

            SingleClassLoader classLoader = new SingleClassLoader(this, signature);
            loaders.put(signature.clazz().getName(), classLoader);
            classLoader.compile();
        }
    }

    /**
     * Stores compile errors for classes that failed to compile and
     * broadcasts errors using Skript's logger.
     *
     * @param failed map of class names and compile errors
     * @param broadcast whether to broadcast the failed classes
     */
    private void loadFailed(Map<String, CompileException> failed, boolean broadcast) {
        failed.keySet().forEach(loaders::remove);
        compileExceptions.putAll(failed);

        if (!broadcast) return;
        failed.forEach((className, exception) -> {
            if (!Storage.contains(className)) return;
            ClassWrapper classWrapper = Storage.of(className).getSourceClass();
            SkriptUtil.error(classWrapper.getNode(), "Class '" + className + "' failed to compile because: " + exception.getMessage());
        });
    }

}
