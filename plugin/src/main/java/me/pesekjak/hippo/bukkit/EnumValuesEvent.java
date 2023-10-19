package me.pesekjak.hippo.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Event for enum constructor calls.
 */
public class EnumValuesEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final List<Object> arguments = new LinkedList<>();

    /**
     * @return arguments used to construct the enum
     */
    public @Unmodifiable List<Object> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    /**
     * @param arguments new arguments to construct the enum
     */
    public void setArguments(Object... arguments) {
        this.arguments.clear();
        this.arguments.addAll(List.of(arguments));
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
