package me.pesekjak.hippo.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprArgument extends SimpleExpression<Object> {

    private int index;

    static {
        Skript.registerExpression(
                ExprArgument.class, Object.class, ExpressionType.SIMPLE,
                "[the] arg[ument][s](-| )<(\\d+)>"
        );
    }

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        if(((MethodCallEvent) event).getArguments().size() < index) return new Object[0];
        return new Object[] { ((MethodCallEvent) event).getArguments().get(index - 1) };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "method argument " + index;
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if(!getParser().isCurrentEvent(MethodCallEvent.class)) return false;
        index = Utils.parseInt(parseResult.regexes.get(0).group());
        return true;
    }
}
