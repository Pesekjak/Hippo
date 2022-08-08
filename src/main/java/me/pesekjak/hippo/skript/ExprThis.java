package me.pesekjak.hippo.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprThis extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(
                ExprThis.class, Object.class, ExpressionType.SIMPLE,
                "this"
        );
    }

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        return new Object[]{((MethodCallEvent) event).getInstance()};
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
        return "this";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        return getParser().isCurrentEvent(MethodCallEvent.class);
    }

}
