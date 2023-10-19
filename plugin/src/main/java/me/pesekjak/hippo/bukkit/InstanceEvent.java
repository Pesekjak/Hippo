package me.pesekjak.hippo.bukkit;

import me.pesekjak.hippo.core.AbstractClass;
import org.jetbrains.annotations.Nullable;

/**
 * Event for sections that can reference the current instance of the class.
 */
public abstract class InstanceEvent extends ThrowableEvent {

    private final @Nullable Object instance;

    protected InstanceEvent(AbstractClass source, @Nullable Object instance) {
        super(source);
        this.instance = instance;
    }

    /**
     * @return current class instance
     */
    public @Nullable Object getInstance() {
        return instance;
    }

}
