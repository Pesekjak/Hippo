package me.pesekjak.hippo.bukkit;

import me.pesekjak.hippo.core.skript.ClassWrapper;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event for new class structure.
 */
public class NewClassEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ClassWrapper wrapper;

    public NewClassEvent(ClassWrapper wrapper) {
        this.wrapper = wrapper;
    }

    /**
     * @return class that is currently being parsed
     */
    public ClassWrapper getParsingClass() {
        return wrapper;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
