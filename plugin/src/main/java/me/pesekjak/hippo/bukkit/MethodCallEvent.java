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

    private @Nullable Object returned;

    public MethodCallEvent(AbstractClass source, @Nullable Object instance) {
        super(source, instance);
    }

    /**
     * @return returned object
     */
    public @Nullable Object getReturned() {
        return returned;
    }

    /**
     * @param returned new returned object
     */
    public void setReturned(@Nullable Object returned) {
        this.returned = returned;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
