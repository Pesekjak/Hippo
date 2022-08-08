package me.pesekjak.hippo.utils.events;

import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import lombok.Getter;
import lombok.Setter;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.classtypes.SkriptEnum;
import me.pesekjak.hippo.classes.content.Constructor;
import me.pesekjak.hippo.classes.content.Method;
import me.pesekjak.hippo.utils.SkriptUtil;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MethodCallEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter @Nullable
    private final ISkriptClass skriptClass;
    @Getter @NotNull
    private final String name;
    @Getter @Nullable
    private final Object instance;
    @Getter @Setter @Nullable
    private Object output;

    private Iterator<String> argumentAliases;

    @Getter
    private final List<Object> arguments = new ArrayList<>();

    /**
     * Used for calling the method and constructor section.
     * @param skriptClass SkriptClass of the method/constructor
     * @param name identifier of the method/constructor
     * @param instance instance of the class (this)
     */
    public MethodCallEvent(@Nullable ISkriptClass skriptClass, @NotNull String name, @Nullable Object instance) {
        this.skriptClass = skriptClass;
        this.name = name;
        this.instance = instance;
        if(skriptClass == null) return;
        if(!(skriptClass.getClassContent(name) instanceof Method method))
            return;
        argumentAliases = method.getArguments().keySet().iterator();

        // Skips first 2 enum constructor arguments, because
        // they're not defined by user
        if(name.startsWith(Constructor.METHOD_NAME) && skriptClass instanceof SkriptEnum) {
            argumentAliases.next();
            argumentAliases.next();
        }
    }

    public MethodCallEvent addArgument(Object argument) {
        arguments.add(argument);
        if(argumentAliases == null) return this;
        String alias = null;
        if(argumentAliases.hasNext())
            alias = argumentAliases.next();
        Variable<?> var = Variable.newInstance("_" + alias, new Class[]{Object.class});
        if(var == null) return this;
        SkriptUtil.setVariable(var, this, argument);
        return this;
    }

    public void trigger() {
        if(skriptClass == null) return;
        if(!(skriptClass.getClassContent(name) instanceof Method method))
            return;
        TriggerItem.walk(method.getTrigger(), this);
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
