package me.pesekjak.hippo.classes.builder;

import me.pesekjak.hippo.hooks.SkriptReflectHook;

import java.util.HashMap;

public class DynamicClassLoader extends ClassLoader {

    public HashMap<String, byte[]> bytecodeMap = new HashMap<>();

    public DynamicClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    public Class<?> loadClass(String name, byte[] bytes) {
        bytecodeMap.put(name, bytes);
        Class<?> newClass = null;
        try {
            newClass = SkriptReflectHook.getLibraryLoader().loadClass(name);
        } catch (Exception ignored) { }
        bytecodeMap.remove(name);
        return newClass;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytecode = bytecodeMap.get(name);
        if(bytecode == null || bytecode.length < 1) {
            return super.findClass(name);
        }
        bytecodeMap.remove(name);
        return defineClass(name, bytecode, 0, bytecode.length);
    }

    public Class<?> tryFindClass(String name) {
        try { return super.findClass(name);
        } catch (ClassNotFoundException ignored) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored1) { }
        }
        return null;
    }

}
