package me.pesekjak.hippo.skript.utils.syntax.operators;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.utils.SkriptUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprBitwiseOp extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprBitwiseOp.class, Number.class, ExpressionType.COMBINED,
                "%number%[ ]&[ ]%number%",
                "%number%[ ]\\|[ ]%number%",
                "%number%[ ]\\^\\^[ ]%number%",
                "~%number%"
        );
    }

    private Expression<Number> first;
    private Expression<Number> second;
    private int pattern;

    @Override
    protected Number @NotNull [] get(@NotNull Event event) {
        if(first == null) return new Number[0];
        if(first.getSingle(event) == null) return new Number[0];
        Number base = first.getSingle(event);
        Number op = 0;
        if(pattern != 3) {
            if(second == null) return new Number[0];
            if(second.getSingle(event) == null) return new Number[0];
            op = second.getSingle(event);
        }
        if(pattern == 0) {
            return new Number[]{base.intValue() & op.intValue()};
        } else if(pattern == 1){
            return new Number[]{base.intValue() | op.intValue()};
        } else if (pattern == 2){
            return new Number[]{base.intValue() ^ op.intValue()};
        } else {
            return new Number[]{~base.intValue()};
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "bitwise operator";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pattern = i;
        first = SkriptUtils.defendExpression(expressions[0]);
        if(pattern != 3){
            second = SkriptUtils.defendExpression(expressions[1]);
        }
        return true;
    }
}
