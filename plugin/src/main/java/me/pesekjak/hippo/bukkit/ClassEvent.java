package me.pesekjak.hippo.bukkit;

import me.pesekjak.hippo.core.AbstractClass;

/**
 * Event that references its source.
 */
public interface ClassEvent {

    /**
     * @return source of the event
     */
    AbstractClass getSource();

}
