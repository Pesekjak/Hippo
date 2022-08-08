package me.pesekjak.hippo.skript;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.JavaType;
import com.btk5h.skriptmirror.skript.custom.CustomSyntaxSection;
import com.btk5h.skriptmirror.util.SkriptMirrorUtil;
import com.btk5h.skriptmirror.util.SkriptUtil;
import lombok.Getter;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.DynamicClassLoader;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreImportSection {

    @Getter
    private static final Pattern IMPORT_STATEMENT =
            Pattern.compile("(" + SkriptMirrorUtil.PACKAGE + ")(?:\\s+as (" + SkriptMirrorUtil.IDENTIFIER + "))?");
    @Getter
    private static final Map<File, Map<String, PreImport>> preImports = new HashMap<>();

    static {
        CustomSyntaxSection.register("Pre-import", SecPreImport.class, "pre[-]import");
        Skript.registerExpression(
                ExprPreImport.class, Object.class, ExpressionType.SIMPLE,
                "<" + SkriptMirrorUtil.IDENTIFIER + ">"
        );
    }

    public static @Nullable PreImport lookup(File script, String alias) {
        Map<String, PreImport> scriptPreImports = preImports.get(script);
        if(scriptPreImports == null) return null;
        else return scriptPreImports.get(alias);
    }

    private static void registerPreImport(String rawStatement, @NotNull File script) {
        Matcher statement = IMPORT_STATEMENT.matcher(ScriptLoader.replaceOptions(rawStatement));
        if (!statement.matches()) {
            Skript.error(rawStatement + " is an invalid import statement.");
            return;
        }

        String cls = statement.group(1);
        String importName = statement.group(2);
        String simpleName = cls.substring(cls.lastIndexOf(".") + 1);

        if (simpleName.equals(importName)) {
            Skript.warning(cls + " doesn't need the alias " + importName + ", as it will already be imported under that name");
        }

        if (importName == null) importName = simpleName;

        preImports.putIfAbsent(script, new HashMap<>());
        preImports.get(script).compute(importName, (name, oldClass) -> {
            if (oldClass != null) {
                Skript.error(name + " is already mapped to " + oldClass.dotPath() + ". " +
                        "It will not be remapped to " + cls + ".");
                return oldClass;
            }
            return new PreImport(cls);
        });
    }

    public static class SecPreImport extends SelfRegisteringSkriptEvent {

        @Override
        public void register(@NotNull Trigger trigger) {

        }

        @Override
        public void unregister(@NotNull Trigger trigger) {
            preImports.remove(trigger.getScript());
        }

        @Override
        public void unregisterAll() {
            preImports.clear();
        }

        @Override
        public boolean init(Literal<?> @NotNull [] literals, int i, SkriptParser.@NotNull ParseResult parseResult) {
            File currentScript = SkriptUtil.getCurrentScript();
            SectionNode node = (SectionNode) SkriptLogger.getNode();

            if(currentScript == null) return false;
            if(node == null || node.getKey() == null) return false;
            if (node.getKey().toLowerCase().startsWith("on ")) return false;

            preImports.put(currentScript, new HashMap<>());

            node.forEach(subNode -> registerPreImport(subNode.getKey(), currentScript));
            SkriptUtil.clearSectionNode(node);

            return true;
        }

        @Override
        public @NotNull String toString(Event event, boolean b) {
            return "pre-import";
        }

    }

    public static class ExprPreImport extends SimpleExpression<Object> {

        protected String classAlias;
        protected PreImport preImport;
        private Node node;

        @Override
        protected Object @NotNull [] get(@NotNull Event event) {
            if(preImport.findClass() != null) return new Object[] {preImport.asJavaType()};
            if(event instanceof NewSkriptClassEvent) {
                return new Object[] {preImport.asType()};
            }
            String reason = DynamicClassLoader.getCurrentClassloader().getFailReasons().get(preImport.asType().dotPath());
            if(reason == null)
                reason = "Class '" + preImport.asType().dotPath() + "' doesn't exist";
            Logger.warn("Your code reached reference of not yet compiled class '" + preImport.asType().dotPath() + "'. " +
                    "Make sure this pre-imported class exists or got compiled without problems: " +
                    node.toString().replaceAll("&[0-9a-f]", "") +
                    " (Expected problem: " + reason + ")");
            return new Object[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public @NotNull Class<? extends JavaType> getReturnType() {
            return JavaType.class;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public @NotNull String toString(Event event, boolean b) {
            if(preImport.findClass() != null) return preImport.findClass().getName();
            Type type = preImport.asType();
            return type.dotPath() != null ? type.dotPath() : type.simpleName();
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            classAlias = parseResult.regexes.get(0).group();
            Config script = getParser().getCurrentScript();
            if(script == null) return false;
            preImport = PreImportSection.lookup(script.getFile(), classAlias);
            node = getParser().getNode();
            return preImport != null;
        }

    }

}
