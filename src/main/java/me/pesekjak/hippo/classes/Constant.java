package me.pesekjak.hippo.classes;

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

    public String toJavaCode() {
        if(constant instanceof String) return "\"" + constant + "\"";
        if(constant instanceof Character) return "'" + constant + "'";
        if(constant != null) return constant.toString();
        if(type != null) return type.getDotPath() + "." + path;
        return null;
    }

}
