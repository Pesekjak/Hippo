package me.pesekjak.hippo.core.loader;

import me.pesekjak.hippo.core.AbstractClass;
import org.jetbrains.annotations.Nullable;

/**
 * Class loader for a single custom class.
 */
public class SingleClassLoader extends ClassLoader {

    private final DynamicClassLoader source;
    private final ClassSignature classSignature;

    private @Nullable Class<?> compiled;

    SingleClassLoader(DynamicClassLoader source, AbstractClass clazz) {
        this.source = source;
        classSignature = new ClassSignature(clazz);
    }

    SingleClassLoader(DynamicClassLoader source, ClassSignature classSignature) {
        this.source = source;
        this.classSignature = classSignature;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.equals(classSignature.clazz().getName())) {
            if (compiled == null) throw new ClassNotFoundException();
            return compiled;
        }
        return source.findClass(name);
    }

    /**
     * @return signature of the class of this class loader
     */
    public ClassSignature getClassSignature() {
        return classSignature;
    }

    /**
     * @return whether the class has been already compiled
     */
    public boolean compiled() {
        return compiled != null;
    }

    /**
     * Compiles the class from the class signature.
     */
    public void compile() throws ClassFormatError, IndexOutOfBoundsException, SecurityException {
        if (compiled != null) return;
        compiled = defineClass(classSignature.clazz().getName(), classSignature.data(), 0, classSignature.data().length);
    }

}
