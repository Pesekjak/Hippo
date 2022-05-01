package me.pesekjak.hippo.utils.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ArgumentsEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final HashMap<Number, Object> arguments;

    public ArgumentsEvent() {
        this.arguments = new HashMap<>();
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
