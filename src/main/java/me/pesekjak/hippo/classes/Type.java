package me.pesekjak.hippo.classes;

import me.pesekjak.hippo.hooks.SkriptReflectHook;

public class Type extends IType {

    private final String dotPath;
    private final String internalName;

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

    @Override
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

    @Override
    public String getRawDescriptor() {
        return descriptor.replace("[", "");
    }

    @Override
    public Type arrayType() {
        return new Type(dotPath, "[" + descriptor, internalName);
    }

    @Override
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

    public org.objectweb.asm.Type toASMType() {
        return org.objectweb.asm.Type.getType(descriptor);
    }

}
