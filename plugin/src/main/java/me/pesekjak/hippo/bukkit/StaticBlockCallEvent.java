package me.pesekjak.hippo.bukkit;

import me.pesekjak.hippo.core.AbstractClass;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event for static block section.
 */
public class StaticBlockCallEvent extends ThrowableEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public StaticBlockCallEvent(AbstractClass source) {
        super(source);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
