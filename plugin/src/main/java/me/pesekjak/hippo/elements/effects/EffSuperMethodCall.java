package me.pesekjak.hippo.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.elements.expressions.ExprSuperMethodCall;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Superclass Method Call")
@Description("Calls method of the parent class.")
@Examples({
        "public class Elephant extends Animal:",
        "\t@Override",
        "\tpublic int value():",
        "\t\tsuper.helloWorld()",
        "\t\treturn 1"
})
@Since("1.1")
@SuppressWarnings("UnstableApiUsage")
// Mirrors behaviour of ExprSuperMethodCall
public class EffSuperMethodCall extends Effect {

    private ExprSuperMethodCall mirror;

    static {
        Hippo.getAddonInstance().syntaxRegistry().register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffSuperMethodCall.class)
                        .addPatterns(ExprSuperMethodCall.PATTERNS)
                        .supplier(EffSuperMethodCall::new)
                        .origin(SyntaxOrigin.of(Hippo.getAddonInstance()))
                        .priority(SyntaxInfo.PATTERN_MATCHES_EVERYTHING)
                        .build()
        );
    }

    @Override
    protected void execute(@NotNull Event event) {
        mirror.getSingle(event);
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "super method call";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        mirror = new ExprSuperMethodCall();
        return mirror.init(expressions, matchedPattern, isDelayed, parseResult);
    }

}
