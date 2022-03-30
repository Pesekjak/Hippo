package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprPrimitive extends SimpleExpression<Primitive> {

    static {
        Skript.registerExpression(ExprPrimitive.class, Primitive.class, ExpressionType.SIMPLE,
                "boolean",
                "char",
                "byte",
                "short",
                "int",
                "float",
                "long",
                "double",
                "void"
        );
    }

    private int pattern;

    @Override
    protected Primitive @NotNull [] get(@NotNull Event event) {
        Primitive primitive = null;
        switch (pattern) {
            case 0 -> primitive = Primitive.BOOLEAN;
            case 1 -> primitive = Primitive.CHAR;
            case 2 -> primitive = Primitive.BYTE;
            case 3 -> primitive = Primitive.SHORT;
            case 4 -> primitive = Primitive.INT;
            case 5 -> primitive = Primitive.LONG;
            case 6 -> primitive = Primitive.FLOAT;
            case 7 -> primitive = Primitive.DOUBLE;
            case 8 -> primitive = Primitive.VOID;
        }
        return new Primitive[] { primitive };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Primitive> getReturnType() {
        return Primitive.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "primitive";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pattern = i;
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
