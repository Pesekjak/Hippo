package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TypeRecord extends SkriptClass {

    private final HashMap<String, Argument> constructorArguments;

    public TypeRecord(@NotNull Type type) {
        super(type, ClassType.RECORD);
        constructorArguments = new HashMap<>();
    }

    @Override
    public String toJavaCode() {
        return null;
    }

    public HashMap<String, Argument> getConstructorArguments() {
        return constructorArguments;
    }

    public void addConstructorArgument(Argument argument) {
        this.constructorArguments.putIfAbsent(argument.getName(), argument);
        this.constructorArguments.replace(argument.getName(), argument);
    }

    public void removeConstructorArgument(String name) {
        this.constructorArguments.remove(name);
    }
}
