package me.pesekjak.hippo.utils.events;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.TriggerItem;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NewSkriptClassEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SkriptClass skriptClass;
    private TriggerItem currentTriggerItem;
    private Node currentNode;
    private List<Annotation> stackedAnnotations = new ArrayList<>();

    public NewSkriptClassEvent(SkriptClass skriptClass) {
        this.skriptClass = skriptClass;
    }

    public SkriptClass getSkriptClass() {
        return skriptClass;
    }

    public Type toType() {
        return skriptClass.getType();
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

    public List<Annotation> getStackedAnnotations() {
        return stackedAnnotations;
    }

    public void addStackingAnnotation(Annotation stackedAnnotation) {
        stackedAnnotations.add(stackedAnnotation);
    }

    public void clearStackedAnnotations() {
        stackedAnnotations.clear();
    }

}
