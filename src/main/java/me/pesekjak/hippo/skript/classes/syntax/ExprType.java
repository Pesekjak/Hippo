package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.skript.classes.ClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprType extends SimpleExpression<Type> {

    static {
        Skript.registerExpression(ExprType.class, Type.class, ExpressionType.COMBINED,
                "%-javatype%(1¦|2¦<^(\\[\\])*>)(3¦|4¦\\.\\.\\.)"
        );
    }

    private Expression<?> javaTypeExpression;
    private int arraySize = 0;
    private int parseMark;

    @Override
    protected Type @NotNull [] get(@NotNull Event event) {
        Type type = null;
        if(javaTypeExpression != null) {
            String className = ClassBuilder.getClassNameFromExpression(javaTypeExpression, event);
            if (className == null) return new Type[0];
            type = new Type(className);
        }
        if(type == null) return new Type[0];
        if(parseMark == 1 || parseMark == 6) {
            for(int i = 0; i < arraySize; ++i) {
                type = type.arrayType();
            }
        }
        if(parseMark == 5 || parseMark == 6) type = type.varArgType();
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
        javaTypeExpression = SkriptUtils.defendExpression(expressions[0]);
        if(parseResult.regexes.size() > 0) {
            arraySize = parseResult.regexes.get(0).group().length() / 2;
        }
        this.parseMark = parseResult.mark;
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && getParser().getCurrentSections().size() == 0;
    }
}
