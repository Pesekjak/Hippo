package me.pesekjak.hippo.classes.classtypes;

import me.pesekjak.hippo.classes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TypeRecord extends SkriptClass {

    private final HashMap<String, Argument> recordConstructorArguments;

    public TypeRecord(@NotNull Type type) {
        super(type, ClassType.RECORD);
        recordConstructorArguments = new HashMap<>();
    }

    public HashMap<String, Argument> getRecordConstructorArguments() {
        return recordConstructorArguments;
    }

    public void addRecordConstructorArgument(Argument argument) {
        this.recordConstructorArguments.putIfAbsent(argument.getName(), argument);
        this.recordConstructorArguments.replace(argument.getName(), argument);
    }

    public void removeRecordConstructorArgument(String name) {
        this.recordConstructorArguments.remove(name);
    }
}
