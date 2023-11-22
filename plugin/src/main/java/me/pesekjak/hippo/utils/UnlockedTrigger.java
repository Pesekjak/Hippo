package me.pesekjak.hippo.utils;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.script.Script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Trigger implementation that allows to set the trigger items, get the first
 * and last trigger item and walk the trigger.
 * <p>
 * Is used by constructor elements to check whether the next trigger item is
 * call to the super class constructor.
 */
public class UnlockedTrigger extends Trigger {

    private final List<TriggerItem> items = new ArrayList<>();

    public UnlockedTrigger(Script script, String name, SkriptEvent event, List<TriggerItem> items) {
        super(script, name, event, items);
        this.items.addAll(items);
    }

    @Override
    public TriggerItem walk(@NotNull Event event) {
        return super.walk(event);
    }

    @Override
    public void setTriggerItems(@NotNull List<TriggerItem> items) {
        super.setTriggerItems(items);
        this.items.clear();
        this.items.addAll(items);
    }

    /**
     * @return all items of this trigger
     */
    public @Unmodifiable List<TriggerItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * @return first item of this trigger
     */
    public @Nullable TriggerItem getFirst() {
        return first;
    }

    /**
     * @return last item of this trigger
     */
    public @Nullable TriggerItem getLast() {
        return last;
    }

}
