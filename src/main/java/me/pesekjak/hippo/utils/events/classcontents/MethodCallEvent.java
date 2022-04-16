package me.pesekjak.hippo.utils.events.classcontents;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MethodCallEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Object instance;
    private final String methodName;
    private final HashMap<Number, Object> arguments;
    private Object output;

    public MethodCallEvent(Object instance, String className) {
        this.instance = instance;
        this.methodName = className;
        this.arguments = new HashMap<>();
    }

    public Object getInstance() {
        return instance;
    }

    public String getMethodName() {
        return methodName;
    }

    public HashMap<Number, Object> getArguments() {
        return arguments;
    }

    public Object getArgument(Number argumentIndex) {
        return arguments.get(argumentIndex);
    }

    public void addArgument(Number argumentIndex, Object argument) {
        arguments.putIfAbsent(argumentIndex, argument);
        arguments.replace(argumentIndex, argument);
    }

    public Object getOutput() {
        return output;
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
