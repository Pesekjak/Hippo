package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Special Expression returning Reflect's JavaType but constructing Hippo's Type
 * in background with array and vararg support that can be returned by calling
 * getSingleType.
 */
public class ExprType extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprType.class, Object.class, ExpressionType.COMBINED,
                "%javatype%(1¦$|2¦<^(\\[\\])*>)(3¦|4¦\\.\\.\\.)"
        );
    }

    private Expression<?> javaTypeExpression;
    private int arraySize = 0;
    private int parseMark;

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        if(javaTypeExpression == null) return new Object[0];
        return new Object[] { javaTypeExpression.getAll(event) };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Object> getReturnType() {
        return SkriptReflectHook.getJavaTypeClass();
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "type";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        javaTypeExpression = SkriptUtils.defendExpression(expressions[0]);
        if(javaTypeExpression.getClass() == SimpleLiteral.class) return false;
        if(parseResult.regexes.size() > 0) {
            arraySize = parseResult.regexes.get(0).group().length() / 2;
        }
        this.parseMark = parseResult.mark;
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && getParser().getCurrentSections().size() == 0;
    }

    public Type getSingleType() {
        Type type = null;
        if(javaTypeExpression != null) {
            type = SkriptClassBuilder.getTypeFromExpression(javaTypeExpression);
            if(type == null) return null;
        }
        if(type == null) return null;
        if(parseMark == 1 || parseMark == 6) {
            for(int i = 0; i < arraySize; ++i) {
                type = type.arrayType();
            }
        }
        if(parseMark == 5 || parseMark == 6) type = type.varArgType();
        return type;
    }

}
