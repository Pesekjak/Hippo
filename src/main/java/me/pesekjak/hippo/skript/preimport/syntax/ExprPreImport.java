package me.pesekjak.hippo.skript.preimport.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.preimport.PreImport;
import me.pesekjak.hippo.preimport.PreImportManager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Expression for PreImported classes, to pre-import a class EvtPreImport and EffPreImport are used.
 * This Expression returns either a Reflect's JavaType or Hippo's Type, depending on existence of the class.
 * If class doesn't exist, Type of pre-imported class is returned, if it does, JavaType of the same
 * class is returned instead.
 */

@Name("PreImported Class")
@Description("Returns a Java Type for a PreImported class.")
@Since("1.0-BETA.1")
public class ExprPreImport extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprPreImport.class, Object.class, ExpressionType.COMBINED,
                "<[a-zA-Z0-9]*>[*]"
        );
    }

    private String classAlias;
    private Config script;

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        Object javaType = null;
        PreImport preImport = PreImportManager.MANAGER.getPreImporting(script).getPreImport(classAlias);
        Type preImportType = preImport.getType();
        try {
            javaType = SkriptReflectHook.buildJavaType(SkriptReflectHook.getLibraryLoader().loadClass(preImportType.getDotPath()));
        } catch (Exception ignored) { }
        if (javaType != null) {
            return new Object[]{javaType};
        }
        return new Object[]{ preImportType };
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
        return classAlias;
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        classAlias = parseResult.regexes.get(0).group();
        script = getParser().getCurrentScript();
        if(script == null) return false;
        if(!PreImportManager.MANAGER.isPreImporting(script)) return false;
        return PreImportManager.MANAGER.getPreImporting(script).containsAlias(classAlias);
    }
}
