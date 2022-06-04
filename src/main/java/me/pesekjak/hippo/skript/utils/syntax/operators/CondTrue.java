package me.pesekjak.hippo.skript.utils.syntax.operators;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.utils.SkriptUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CondTrue extends Condition {

    static {
        Skript.registerCondition(CondTrue.class,
                "%booleans% \\?"
        );
    }

    private Expression<Boolean> booleanExpression;

    @Override
    public boolean check(@NotNull Event event) {
        if(booleanExpression == null) return false;
        return !Arrays.stream(booleanExpression.getAll(event)).toList().contains(false);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "boolean condition";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        booleanExpression = SkriptUtils.defendExpression(expressions[0]);
        return true;
    }

}
