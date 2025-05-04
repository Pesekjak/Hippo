package me.pesekjak.hippo.elements.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import com.btk5h.skriptmirror.util.SkriptMirrorUtil;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.core.PreImport;
import me.pesekjak.hippo.elements.expressions.ExprPreImport;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.TypeLookup;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Pre-Import")
@Description("Pre-imports new classes to the script.")
@Examples({
        "pre-import:",
        "\thippo.Blob",
        "\tjava.lang.RuntimeException as RTE"
})
@Since("1.0.0")
@SuppressWarnings("UnstableApiUsage")
public class StructPreImport extends Structure {

    private static final Pattern IMPORT_STATEMENT = Pattern.compile(
            "(" + SkriptMirrorUtil.PACKAGE + ")(?:\\s+as (" + SkriptMirrorUtil.IDENTIFIER + "))?"
    );

    private Script script;

    static {
        Hippo.getAddonInstance().syntaxRegistry().register(
                SyntaxRegistry.STRUCTURE,
                SyntaxInfo.Structure.builder(StructPreImport.class)
                        .addPattern("pre[-]import")
                        .supplier(StructPreImport::new)
                        .origin(SyntaxOrigin.of(Hippo.getAddonInstance()))
                        .priority(SyntaxInfo.SIMPLE)
                        .build()
        );
    }

    @Override
    public boolean init(Literal<?> @NotNull [] args,
                        int matchedPattern,
                        SkriptParser.@NotNull ParseResult parseResult,
                        @Nullable EntryContainer entryContainer) {
        script = SkriptUtil.getCurrentScript(getParser());
        if (script == null) return false;
        if (entryContainer == null) return false;
        SectionNode node = entryContainer.getSource();

        node.forEach(subNode -> registerPreImport(subNode.getKey(), script));
        ExprPreImport.updateSyntax();

        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public Priority getPriority() {
        return new Priority(200);
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        if (script == null) return "pre-import";
        return "pre-import of " + script.getConfig().getFileName();
    }

    private void registerPreImport(String rawStatement, Script script) {
        Matcher statement = IMPORT_STATEMENT.matcher(ScriptLoader.replaceOptions(rawStatement));
        if (!statement.matches()) {
            Skript.error(rawStatement + " is an invalid import statement.");
            return;
        }

        String clazz = statement.group(1);
        String importName = statement.group(2);
        String simpleName = clazz.substring(clazz.lastIndexOf(clazz.contains("$") ? "$" : ".") + 1);

        if (simpleName.equals(importName))
            Skript.warning(clazz + " doesn't need the alias " + importName + ", as it will already be imported under that name");

        if (importName == null) importName = simpleName;

        Type type = TypeLookup.lookup(script, importName, false);
        if (type != null) {
            Skript.error(importName + " is already mapped to " + type.getClassName() + ". "
                    + "It will not be remapped to " + clazz + ".");
            return;
        }

        TypeLookup.registerPreImport(script, importName, PreImport.fromDotPath(clazz));
    }

}
