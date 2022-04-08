package me.pesekjak.hippo.classes.registry;

import me.pesekjak.hippo.classes.SkriptClass;

import java.util.HashMap;

public class SkriptClassRegistry {

    public static final SkriptClassRegistry REGISTRY = new SkriptClassRegistry();
    public final HashMap<String, SkriptClass> skriptClassMap = new HashMap<>();

    private SkriptClassRegistry() { }

    public SkriptClass getSkriptClass(String className) {
        return skriptClassMap.get(className);
    }

    public void addSkriptClass(String className, SkriptClass skriptClass) {
        skriptClassMap.putIfAbsent(className, skriptClass);
        skriptClassMap.replace(className, skriptClass);
    }

    public void clear() {
        skriptClassMap.clear();
    }

}
