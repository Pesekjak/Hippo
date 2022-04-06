package me.pesekjak.hippo.skript.preimport.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.preimport.PreImportManager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprPreImport extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprPreImport.class, Object.class, ExpressionType.COMBINED,
                "<[a-zA-Z0-9]*>"
        );
    }

    private String classAlias;
    private Config script;

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        Object javaType = SkriptReflectHook.buildJavaType(PreImportManager.MANAGER.getPreImporting(script).getPreImport(classAlias).getType().findClass());
        if(javaType != null) {
            return new Object[] { javaType };
        }
        return new Object[] { PreImportManager.MANAGER.getPreImporting(script).getPreImport(classAlias).getType() };
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
    public @NotNull String toString(Event event, boolean b) {
        return "$" + classAlias;
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        classAlias = parseResult.regexes.get(0).group();
        script = getParser().getCurrentScript();
        if(!PreImportManager.MANAGER.isPreImporting(script)) return false;
        return PreImportManager.MANAGER.getPreImporting(script).containsAlias(classAlias);
    }
}
