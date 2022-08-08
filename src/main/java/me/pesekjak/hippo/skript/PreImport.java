package me.pesekjak.hippo.skript;

import com.btk5h.skriptmirror.JavaType;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PreImport(String dotPath) {

    public @NotNull Type asType() {
        return new NonPrimitiveType(dotPath);
    }

    public @Nullable Class<?> findClass() {
        return asType().findClass();
    }

    public @Nullable JavaType asJavaType() {
        Class<?> classObject = findClass();
        if(classObject == null) return null;
        return new JavaType(classObject);
    }

}
