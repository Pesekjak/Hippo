package me.pesekjak.hippo.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.JavaType;
import com.btk5h.skriptmirror.util.SkriptMirrorUtil;
import me.pesekjak.hippo.bukkit.NewClassEvent;
import me.pesekjak.hippo.core.ASMUtil;
import me.pesekjak.hippo.core.PreImport;
import me.pesekjak.hippo.core.loader.CompileException;
import me.pesekjak.hippo.core.loader.DynamicClassLoader;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.TypeLookup;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.script.Script;

@Name("Pre-Imported Class")
@Description("Reference of pre-imported class.")
@Examples({
        "pre-import:",
        "\thippo.Blob",
        "on load:",
        "\tset {_blob} to new Blob()"
})
@Since("1.0.0")
public class ExprPreImport extends SimpleExpression<Object> {

    private PreImport preImport;
    private Node node;

    static {
        // Object class is used so SimpleExpression#get(Event) can return PreImports
        Skript.registerExpression(
                ExprPreImport.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
                "<" + SkriptMirrorUtil.IDENTIFIER + ">"
        );
    }

    // This expression will return PreImport instance if the pre-imported class does not exist
    // and the source event is NewClassEvent used by class elements, if any other event is used
    // JavaType is returned if it can exist, else none and sends a warning.
    // This design allows class elements to get the Type even if the JavaType
    // can not exist yet (during pre-compile time).
    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        if (event instanceof NewClassEvent) return new PreImport[] {preImport};
        if (preImport.asJavaType() == null) {
            String compileError = DynamicClassLoader.getInstance().getCompileError(preImport.toString());
            String errorMessage = "Your code reached reference of not yet compiled class '" + preImport.type().getClassName() + "'. "
                    + "Make sure this pre-imported class got compiled without problems. (Expected problem: %s)"
                    .formatted(compileError != null ? compileError : CompileException.NOT_EXISTING);
            SkriptUtil.warning(node, errorMessage);
            return new JavaType[0];
        }
        return new JavaType[] {preImport.asJavaType()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    // This expression should behave as a java type expression, it can cause cast exceptions if
    // NewClassEvent is used to get the value and the class of the returned object is not checked.
    @Override
    public @NotNull Class<JavaType> getReturnType() {
        return JavaType.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return preImport.type().getClassName();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        Script script = SkriptUtil.getCurrentScript(getParser());
        if (script == null) return false;
        String alias = parseResult.regexes.get(0).group();
        Type type = TypeLookup.lookup(script, alias, false);
        if (type == null) return false;
        if (!(ASMUtil.isComplex(type) && !ASMUtil.isArray(type))) return false;
        preImport = new PreImport(type);
        node = getParser().getNode();
        return true;
    }

}
