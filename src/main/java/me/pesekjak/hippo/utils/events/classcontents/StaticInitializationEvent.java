package me.pesekjak.hippo.utils.events.classcontents;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StaticInitializationEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
