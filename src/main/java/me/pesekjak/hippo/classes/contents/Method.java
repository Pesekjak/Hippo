package me.pesekjak.hippo.classes.contents;

import ch.njol.skript.lang.Trigger;
import me.pesekjak.hippo.classes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Method extends Annotatable {

    private final Primitive primitive;
    private final Type type;
    private final String name;

    private final List<Argument> arguments;

    private boolean isRunnable;

    private Constant defaultConstant;

    private Trigger trigger;

    public Method(@NotNull Primitive primitive, @Nullable Type type, @NotNull String name) {
        this.primitive = primitive;
        this.type = type;
        this.name = name;
        this.arguments = new ArrayList<>();
        this.isRunnable = false;
    }

    public Primitive getPrimitive() {
        return primitive;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public void removeArgument(Argument argument) {
        arguments.remove(argument);
    }

    public boolean isRunnable() {
        return isRunnable;
    }

    public void setRunnable(boolean runnable) {
        isRunnable = runnable;
    }

    public Constant getDefaultConstant() {
        return defaultConstant;
    }

    public void setDefaultConstant(Constant defaultConstant) {
        this.defaultConstant = defaultConstant;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public String toJavaCode(SkriptClass skriptClass) {
        return null;
    }
}
