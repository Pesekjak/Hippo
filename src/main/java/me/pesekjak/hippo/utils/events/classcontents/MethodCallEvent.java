package me.pesekjak.hippo.utils.events.classcontents;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MethodCallEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Object instance;
    private final String methodName;
    private final HashMap<String, Object> arguments;
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

    public HashMap<String, Object> getArguments() {
        return arguments;
    }

    public Object getArgument(String argumentName) {
        return arguments.get(argumentName);
    }

    public void addArgument(String argumentName, Object argument) {
        arguments.putIfAbsent(argumentName, argument);
        arguments.replace(argumentName, argument);
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
