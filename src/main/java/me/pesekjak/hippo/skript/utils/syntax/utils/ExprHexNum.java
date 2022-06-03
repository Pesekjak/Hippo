package me.pesekjak.hippo.skript.utils.syntax.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprHexNum extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprHexNum.class, Number.class, ExpressionType.COMBINED,
                "<0[xX][0-9a-fA-F]+>"
        );
    }

    private Integer number;

    @Override
    protected Number @NotNull [] get(@NotNull Event event) {
        return new Number[]{number};
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
        return "hexadecimal number";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        number = Integer.parseInt(parseResult.regexes.get(0).group().substring(2),16);
        return true;
    }

}
