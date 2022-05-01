package me.pesekjak.hippo.utils.events.classcontents;

import me.pesekjak.hippo.utils.events.ArgumentsEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ConstructorEvent extends ArgumentsEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Object instance;

    public ConstructorEvent(Object instance) {
        this.instance = instance;
    }

    public Object getInstance() {
        return instance;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
