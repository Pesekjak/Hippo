package me.pesekjak.hippo.skript.utils.syntax.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public class ExprArray extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprArray.class, Object.class, ExpressionType.COMBINED,
                "new array %javatype%<^(\\[\\])*>\\(%number%\\)\\(%-objects%\\)"
        );
    }

    private Expression<?> javaTypeExpression;
    private Expression<Number> sizeExpression;
    private Expression<Object> objectsExpression;
    private int arraySize = 0;

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        if(javaTypeExpression == null) return new Object[0];
        if(sizeExpression.getSingle(event) == null) return new Object[0];
        Type type = SkriptClassBuilder.getTypeFromExpression(javaTypeExpression);
        if(type == null) return new Object[0];
        Class<?> typeClass = null;
        try {
            typeClass = SkriptReflectHook.getLibraryLoader().loadClass(type.getDotPath());
        } catch (ClassNotFoundException e) { return new Object[0]; }
        if(sizeExpression.getSingle(event) == null) return new Object[0];
        for (int i = 0; i < arraySize - 1; ++i) {
            typeClass = typeClass.arrayType();
        }
        Object array = Array.newInstance(typeClass, sizeExpression.getSingle(event).intValue());
        int i = 0;
        for(Object object : objectsExpression.getAll(event)) {
            Array.set(array, i, typeClass.cast(SkriptReflectHook.unwrap(object)));
            i++;
        }
        return new Object[] {SkriptReflectHook.wrap(array)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return SkriptReflectHook.getObjectWrapperClass();
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "java array";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        javaTypeExpression = SkriptUtils.defendExpression(expressions[0]);
        sizeExpression = SkriptUtils.defendExpression(expressions[1]);
        objectsExpression = SkriptUtils.defendExpression(expressions[2]);
        if(parseResult.regexes.size() > 0) {
            arraySize = parseResult.regexes.get(0).group().length() / 2;
        }
        return true;
    }
}
