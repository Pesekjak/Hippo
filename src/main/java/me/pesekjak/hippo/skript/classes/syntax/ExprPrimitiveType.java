package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.PrimitiveType;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprPrimitiveType extends SimpleExpression<PrimitiveType> {

    static {
        Skript.registerExpression(ExprPrimitiveType.class, PrimitiveType.class, ExpressionType.COMBINED,
                "*%primitive%(1¦|2¦<^(\\[\\])*>)(3¦|4¦\\.\\.\\.)"
        );
    }

    private Expression<Primitive> primitiveExpression;
    private int arraySize = 0;
    private int parseMark;

    @Override
    protected PrimitiveType @NotNull [] get(@NotNull Event event) {
        PrimitiveType type = null;
        if(primitiveExpression != null) {
            type = new PrimitiveType(primitiveExpression.getSingle(event));
        }
        if(type == null) return new PrimitiveType[0];
        if(parseMark == 1 || parseMark == 6) {
            for(int i = 0; i < arraySize; ++i) {
                type = type.arrayType();
            }
        }
        if(parseMark == 5 || parseMark == 6) type = type.varArgType();
        return new PrimitiveType[] { type };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends PrimitiveType> getReturnType() {
        return PrimitiveType.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "primitive type";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        primitiveExpression = SkriptUtils.defendExpression(expressions[0]);
        if(parseResult.regexes.size() > 0) {
            arraySize = parseResult.regexes.get(0).group().length() / 2;
        }
        this.parseMark = parseResult.mark;
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && getParser().getCurrentSections().size() == 0;
    }
}
