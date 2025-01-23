package me.pesekjak.hippo.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SectionSkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.bukkit.*;
import me.pesekjak.hippo.elements.sections.SecMethod;
import me.pesekjak.hippo.utils.SkriptUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

@Name("This Instance")
@Description("Reference to the current class instance variable.")
@Examples({
        "public class Blob:",
        "\tprivate String name",
        "\tpublic Blob(String name):",
        "\t\tsuper[]()",
        "\t\tset this.name to {_name}"
})
@Since("1.0.0")
@SuppressWarnings("UnstableApiUsage")
public class ExprThis extends SimpleExpression<Object> {

    private Node node;

    static {
        Hippo.getAddonInstance().syntaxRegistry().register(
                SyntaxRegistry.EXPRESSION,
                SyntaxInfo.Expression.builder(ExprThis.class, Object.class)
                        .addPattern("this")
                        .supplier(ExprThis::new)
                        .origin(SyntaxOrigin.of(Hippo.getAddonInstance()))
                        .priority(SyntaxInfo.SIMPLE)
                        .build()
        );
    }

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        if (event instanceof ConstructorSuperCallEvent)
            SkriptUtil.warning(node, "Cannot reference 'this' before supertype constructor has been called");
        if (event instanceof FieldCallEvent fieldCall && fieldCall.getInstance() == null)
            SkriptUtil.warning(node, "Cannot reference 'this' from static context");
        if (!(event instanceof InstanceEvent instanceEvent)) return new Object[0];
        return new Object[] {instanceEvent.getInstance()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "this";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        node = getParser().getNode();

        if (getParser().getCurrentStructure() instanceof SectionSkriptEvent event) {
            if (event.getSection() instanceof SecMethod methodSection && (methodSection.getModifier() & ACC_STATIC) != 0) {
                Skript.error("Cannot reference 'this' from static context");
                return false;
            }
        }

        return getParser().isCurrentEvent(InstanceEvent.class, NewClassEvent.class); // new class event for fields
    }

    @Override
    public Class<?> @NotNull [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return new Class<?>[0];
    }
}
