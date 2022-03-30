package me.pesekjak.hippo.classes;

import me.pesekjak.hippo.hooks.SkriptReflectHook;

public class Type {

    public final String dotPath;
    public final String descriptor;
    public final String internalName;

    public boolean isVarArg = false;

    public Type(String dotPath, String descriptor, String internalName) {
        this.dotPath = dotPath;
        this.descriptor = descriptor;
        this.internalName = internalName;
    }

    public Type(String dotPath) {
        this(dotPath, "L" + dotPath.replace(".", "/") + ";", dotPath.replace(".", "/"));
    }

    public Type(Object javaType) {
        this(SkriptReflectHook.classOfJavaType(javaType).getName());
    }

    public Type(Class<?> classObject) {
        this(classObject.getName());
    }

    public String getDotPath() {
        return dotPath;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getSimpleName() {
        if (descriptor.startsWith("[")) {
            return internalName.substring(internalName.lastIndexOf('/') + 1, internalName.length() - 1) + arrayBlocks();
        }
        return internalName.substring(internalName.lastIndexOf('/') + 1);
    }

    private String arrayBlocks() {
        StringBuilder builder = new StringBuilder();
        String input = descriptor;
        int x;
        while ((x = input.indexOf("[")) > -1) {
            input = input.substring(x + 1);
            builder.append("[]");
        }
        return builder.toString();
    }

    public Type arrayType() {
        return new Type(dotPath, "[" + descriptor, internalName);
    }

    public boolean isArray() {
        return descriptor.startsWith("[");
    }

    public void setVarArg(boolean varArg) {
        isVarArg = varArg;
    }

    public boolean isVarArg() {
        return isVarArg;
    }

    public Type varArgType() {
        Type type = new Type(dotPath, descriptor, internalName);
        type.setVarArg(true);
        return type;
    }

    public Class<?> findClass() {
        try {
            return Class.forName(dotPath);
        } catch (ClassNotFoundException ignored) { }
        return null;
    }

}
