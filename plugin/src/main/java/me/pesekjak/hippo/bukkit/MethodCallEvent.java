package me.pesekjak.hippo.bukkit;

import me.pesekjak.hippo.core.AbstractClass;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event for method sections.
 */
public class MethodCallEvent extends InstanceEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public MethodCallEvent(AbstractClass source, @Nullable Object instance) {
        super(source, instance);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
