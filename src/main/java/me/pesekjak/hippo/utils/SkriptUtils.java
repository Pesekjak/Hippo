package me.pesekjak.hippo.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public class SkriptUtils {

    private static final boolean
            skript2_6 = Skript.classExists("ch.njol.skript.lang.parser.ParserInstance") || !Skript.methodExists(ParserInstance.class, "get"),
            skriptRunning = Bukkit.getPluginManager().isPluginEnabled("Skript");

    public static boolean isSkript2_6() {
        return skript2_6;
    }

    public static boolean isSkriptRunning() {
        return skriptRunning;
    }

    public static void setVariable(Variable<?> variable, Event e, Object o) {
        Variables.setVariable(variable.getName().toString(e), o, e, variable.isLocal());
    }

    public static boolean canInit(Expression<?>... expressions) {
        return LiteralUtils.canInitSafely(expressions);
    }

    public static <T> Expression<T> defendExpression(Expression<?> expr) {
        if (expr instanceof UnparsedLiteral) {
            Literal<?> parsed = ((UnparsedLiteral) expr).getConvertedExpression(Object.class);
            return (Expression<T>) (parsed == null ? expr : parsed);
        } else if (expr instanceof ExpressionList) {
            Expression<?>[] exprs = ((ExpressionList<?>) expr).getExpressions();
            for (int i = 0; i < exprs.length; i++) {
                exprs[i] = defendExpression(exprs[i]);
            }
        }
        return (Expression<T>) expr;
    }

}
