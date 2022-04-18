package me.pesekjak.hippo.utils.events.classcontents;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ConstructorEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Object instance;
    private final HashMap<Number, Object> arguments;

    public ConstructorEvent(Object instance) {
        this.instance = instance;
        this.arguments = new HashMap<>();
    }

    public Object getInstance() {
        return instance;
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

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
