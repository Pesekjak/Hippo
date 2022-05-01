package me.pesekjak.hippo.classes;

import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.utils.Reflectness;
import org.jetbrains.annotations.Nullable;

public class Constant {

    private final Object constant;
    private final Type type;
    private final String path;

    public Constant(@Nullable Object constant, @Nullable Type type, @Nullable String path) {
        this.constant = constant;
        this.type = type;
        this.path = path;
    }

    public Constant(Object constant) {
        this(constant, null, null);
    }

    public Constant(Type type, String path) {
        this(null, type, path);
    }

    public Object getConstantObject() {
        return constant;
    }

    public Object getConstantObject(Primitive primitive) {
        if(primitive == Primitive.NONE) return constant;
        Object primitiveValue = constant;
        switch (primitive) {
            case BOOLEAN -> primitiveValue = ((Boolean) constant).booleanValue();
            case CHAR -> ((Character) constant).charValue();
            case BYTE ->  ((Number) constant).byteValue();
            case SHORT -> ((Number) constant).shortValue();
            case INT -> primitiveValue = ((Number) constant).intValue();
            case FLOAT -> primitiveValue = ((Number) constant).floatValue();
            case LONG -> primitiveValue = ((Number) constant).longValue();
            case DOUBLE -> primitiveValue = ((Number) constant).doubleValue();
        }
        return primitiveValue;
    }

    public Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public Object getTypeConstantObject() {
        Class<?> constantClass = null;
        try {
            constantClass = SkriptReflectHook.getLibraryLoader().loadClass(type.getDotPath());
            return Reflectness.getField(path, constantClass, null);
        } catch (Exception e) {
            return null;
        }
    }

}
