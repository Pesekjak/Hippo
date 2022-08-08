package me.pesekjak.hippo.classes.types;

import com.btk5h.skriptmirror.JavaType;
import com.btk5h.skriptmirror.LibraryLoader;
import me.pesekjak.hippo.classes.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

public class NonPrimitiveType implements Type {

    @NotNull
    private final String dotPath;
    @NotNull
    private final String descriptor;
    @NotNull
    private final String internalName;

    protected NonPrimitiveType(@NotNull String dotPath, @NotNull String descriptor, @NotNull String internalName) {
        this.dotPath = dotPath;
        this.descriptor = descriptor;
        this.internalName = internalName;
    }

    public NonPrimitiveType(String dotPath) {
        this(dotPath, "L" + dotPath.replace(".", "/") + ";", dotPath.replace(".", "/"));
    }

    public NonPrimitiveType(JavaType javaType) {
        this(javaType.getJavaClass().getName());
    }

    public NonPrimitiveType(Class<?> classObject) {
        this(classObject.getName());
    }

    @Override
    public @Nullable String dotPath() {
        return dotPath;
    }

    @Override
    public @NotNull String descriptor() {
        return descriptor;
    }

    @Override
    public @Nullable String internalName() {
        return internalName;
    }

    @Override
    public @NotNull String simpleName() {
        return dotPath.substring(dotPath.lastIndexOf(".") + 1);
    }

    @Override
    public Type array() {
        return new NonPrimitiveType(dotPath, "[" + descriptor, internalName);
    }

    @Override
    public boolean isArray() {
        return descriptor.startsWith("[");
    }

    @Override
    public int loadCode() {
        return Opcodes.ALOAD;
    }

    @Override
    public int storeCode() {
        return Opcodes.ASTORE;
    }

    @Override
    public int returnCode() {
        return Opcodes.ARETURN;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public org.objectweb.asm.Type toASM() {
        return org.objectweb.asm.Type.getType(descriptor);
    }

    @Override
    public @Nullable Class<?> findClass() {
        try {
            return LibraryLoader.getClassLoader().loadClass(isArray() ? descriptor : dotPath);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

}
