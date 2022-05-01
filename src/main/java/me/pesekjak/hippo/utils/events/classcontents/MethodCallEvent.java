package me.pesekjak.hippo.utils.events.classcontents;

import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.utils.events.ArgumentsEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MethodCallEvent extends ArgumentsEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Object instance;
    private final String methodName;
    private Object output;

    public MethodCallEvent(Object instance, String methodName) {
        this.instance = instance;
        this.methodName = methodName;
    }

    public Object getInstance() {
        return instance;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object getOutput() {
        return SkriptReflectHook.unwrap(output);
    }

    public void setOutput(Object output) {
        this.output = output;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
