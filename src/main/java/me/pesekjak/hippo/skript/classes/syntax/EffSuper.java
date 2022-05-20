package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.classcontents.constructors.InitEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EffSuper extends Effect {

    static {
        Skript.registerEffect(EffSuper.class,
                "super \\([%-objects%]\\)"
        );
    }

    private Expression<Object> argumentExpression;

    @Override
    protected void execute(@NotNull Event event) {
        int i = 0;
        if(argumentExpression == null) return;
        for(Object object : argumentExpression.getAll(event)) {
            i++;
            if(SkriptReflectHook.getReflectNullClass().isInstance(object)) object = null;
            ((InitEvent) event).addSuperResult(i, object);
        }
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "super";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        argumentExpression = SkriptUtils.defendExpression(expressions[0]);
        if(!getParser().isCurrentEvent(InitEvent.class)) return false;
        if(SkriptClassBuilder.getRegisteringClass().getClassType() == ClassType.ENUM) {
            Skript.error("You can't call super for enum class");
            return false;
        }
        return true;
    }
}
