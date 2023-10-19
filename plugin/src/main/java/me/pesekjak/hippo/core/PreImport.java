package me.pesekjak.hippo.core;

import com.btk5h.skriptmirror.JavaType;
import me.pesekjak.hippo.core.loader.DynamicClassLoader;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * Represents pre-imported class that might not yet exist.
 *
 * @param type type of the class
 */
public record PreImport(Type type) {

    public PreImport {
        Objects.requireNonNull(type);
    }

    public PreImport(Class<?> clazz) {
        this(Type.getType(clazz));
    }

    public PreImport(String typeDescriptor) {
        this(Type.getType(typeDescriptor));
    }

    public static PreImport fromDotPath(String dotPath) {
        return new PreImport(ASMUtil.getDescriptor(dotPath));
    }

    /**
     * @return returns the pre-import as a Class object or null if the class does not exist
     */
    public @Nullable Class<?> asClass() {
        try {
            return DynamicClassLoader.getInstance().loadClass(type.getClassName());
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * @return returns the pre-import as a JavaType or null if the class does not exist
     */
    public @Nullable JavaType asJavaType() {
        Class<?> clazz = asClass();
        return clazz != null ? new JavaType(clazz) : null;
    }

}
