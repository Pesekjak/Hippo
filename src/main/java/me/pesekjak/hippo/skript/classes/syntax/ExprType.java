package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Expression that converts Reflect's JavaType to Hippo's Type and
 * adds array and vararg information to it.
 */
public class ExprType extends SimpleExpression<Type> {

    static {
        Skript.registerExpression(ExprType.class, Type.class, ExpressionType.COMBINED,
                "%javatype%<^(\\[\\])*>(1¦|2¦\\.\\.\\.)",
                "%javatype%\\.\\.\\."
        );
    }

    private Expression<?> javaTypeExpression;
    private int arraySize = 0;
    private int parseMark;
    private int pattern;

    @Override
    protected Type @NotNull [] get(@NotNull Event event) {
        if(javaTypeExpression == null) return new Type[0];
        Type type = SkriptClassBuilder.getTypeFromExpression(javaTypeExpression);
        if(type == null) return new Type[0];
        if(pattern == 0) {
            for (int i = 0; i < arraySize; ++i) {
                type = type.arrayType();
            }
            if(parseMark == 2) type = type.varArgType();
        } else {
            type = type.varArgType();
        }
        return new Type[] { type };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Type> getReturnType() {
        return Type.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "type";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pattern = i;
        javaTypeExpression = SkriptUtils.defendExpression(expressions[0]);
        if(javaTypeExpression.getClass() == SimpleLiteral.class) return false;
        if(parseResult.regexes.size() > 0) {
            arraySize = parseResult.regexes.get(0).group().length() / 2;
        }
        this.parseMark = parseResult.mark;
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && getParser().getCurrentSections().size() == 0;
    }

}
