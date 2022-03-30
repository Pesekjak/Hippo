package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprPair extends SimpleExpression<Pair> {

    static {
        Skript.registerExpression(ExprPair.class, Pair.class, ExpressionType.COMBINED,
                "(%-primitive%|%-type%) <[a-zA-Z0-9]*>"
        );
    }

    private Expression<Type> typeExpression;
    private Expression<Primitive> primitiveExpression;
    private String name;

    @Override
    protected Pair @NotNull [] get(@NotNull Event event) {
        Type type = null;
        Primitive primitive = Primitive.NONE;
        if(typeExpression != null) type = typeExpression.getSingle(event);
        if(primitiveExpression != null) primitive = primitiveExpression.getSingle(event);
        return new Pair[] { new Pair(primitive, type, name) };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Pair> getReturnType() {
        return Pair.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "pair";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        primitiveExpression = SkriptUtils.defendExpression(expressions[0]);
        typeExpression = SkriptUtils.defendExpression(expressions[1]);
        name = parseResult.regexes.get(0).group();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
