package me.pesekjak.hippo.bukkit;

import me.pesekjak.hippo.core.AbstractClass;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Event for sections that can throw an exception.
 */
public abstract class ThrowableEvent extends Event implements ClassEvent {

    private final AbstractClass source;
    private @Nullable Throwable throwable;

    protected ThrowableEvent(AbstractClass source) {
        this.source = source;
    }

    /**
     * @return thrown exception
     */
    public @Nullable Throwable getThrowable() {
        return throwable;
    }

    /**
     * @param throwable new thrown exception
     */
    public void setThrowable(@Nullable Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public AbstractClass getSource() {
        return source;
    }

}
