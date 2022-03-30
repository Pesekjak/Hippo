package me.pesekjak.hippo.utils.events;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.TriggerItem;
import me.pesekjak.hippo.classes.Type;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NewSkriptClassEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final String className;
    private TriggerItem currentTriggerItem;
    private Node currentNode;

    public NewSkriptClassEvent(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public TriggerItem getCurrentTriggerItem() {
        return currentTriggerItem;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    public void setCurrentTriggerItem(TriggerItem currentTriggerItem) {
        this.currentTriggerItem = currentTriggerItem;
    }

    public Type toType() {
        return new Type(className);
    }

    public Event getEvent() {
        return this;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
