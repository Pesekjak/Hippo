package me.pesekjak.hippo.classes.contents;

import ch.njol.skript.lang.Trigger;
import me.pesekjak.hippo.classes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Method extends Annotatable {

    private final PrimitiveType primitiveType;
    private final Type type;
    private final String name;

    private final List<Argument> arguments;
    private final List<Type> exceptions;

    private boolean isRunnable;

    private Constant defaultConstant;

    private Trigger trigger;

    public Method(@NotNull PrimitiveType primitive, @Nullable Type type, @NotNull String name) {
        this.primitiveType = primitive;
        this.type = type;
        this.name = name;
        this.arguments = new ArrayList<>();
        this.exceptions = new ArrayList<>();
        this.isRunnable = false;
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
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

    public List<Type> getExceptions() {
        return exceptions;
    }

    public void addException(Type exception) {
        exceptions.add(exception);
    }

    public void removeException(Type exception) {
        exceptions.remove(exception);
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

    public boolean hasVarArg() {
        if(arguments.size() == 0) return false;
        Argument last = arguments.get(arguments.size() - 1);
        if(last.getType() != null) {
            return last.getType().isVarArg();
        } else if(last.getPrimitiveType() != null) {
            return last.getPrimitiveType().isVarArg();
        }
        return false;
    }

    public String getDescriptor() {
        String returnDescriptor = this.getType() != null ? this.getType().getDescriptor() : this.getPrimitiveType().getDescriptor();
        StringBuilder argumentsDescriptor = new StringBuilder();
        for(Argument argument : arguments) {
            argumentsDescriptor.append(argument.getType() != null ? argument.getType().getDescriptor() : argument.getPrimitiveType().getDescriptor());
        }
        return "(" + argumentsDescriptor + ")" + returnDescriptor;
    }
}
