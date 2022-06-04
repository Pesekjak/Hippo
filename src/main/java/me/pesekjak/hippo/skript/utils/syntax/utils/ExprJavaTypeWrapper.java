package me.pesekjak.hippo.skript.utils.syntax.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("Java Type Wrapper")
@Description("Wrapper expression for Java Types.")
@Since("1.0-BETA.1")
public class ExprJavaTypeWrapper extends WrapperExpression<Object> {

    static {
        Skript.registerExpression(ExprJavaTypeWrapper.class, Object.class, ExpressionType.COMBINED,
                "%javatype%*"
        );
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "java type wrapper expression";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return SkriptReflectHook.getJavaTypeClass();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr(expressions[0]);
        return true;
    }
}
