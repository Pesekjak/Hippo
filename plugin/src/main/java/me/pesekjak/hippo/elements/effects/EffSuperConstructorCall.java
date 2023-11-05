package me.pesekjak.hippo.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.bukkit.ConstructorCallEvent;
import me.pesekjak.hippo.bukkit.ConstructorSuperCallEvent;
import me.pesekjak.hippo.elements.sections.SecConstructor;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.parser.SuperSignatureParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Name("Superclass Constructor Call")
@Description("Calls constructor of the parent class.")
@Examples({
        "public class Elephant extends Animal:",
        "\tpublic Elephant():",
        "\t\tsuper[String](\"elephant\")"
})
@Since("1.0.0")
public class EffSuperConstructorCall extends Effect {

    private List<Type> types;
    private Expression<Object> values;

    static {
        Skript.registerEffect(
                EffSuperConstructorCall.class,
                "super\\[[<.+>]\\]\\([%-objects%]\\)"
        );
    }

    @Override
    public void execute(@NotNull Event event) {
        if (!(event instanceof ConstructorSuperCallEvent callEvent)) return;
        if (getValues() == null) return;
        callEvent.setArguments(getValues().getAll(event));
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "super constructor call";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        if (!getParser().isCurrentEvent(ConstructorCallEvent.class)) return false;
        if (!(getParser().getCurrentStructure() instanceof SectionSkriptEvent event)) return false;
        if (!(event.getSection() instanceof SecConstructor)) return false;

        Node thisNode = getParser().getNode();
        if (thisNode == null || thisNode.getParent() == null) return false;
        Iterator<Node> nodes = thisNode.getParent().iterator();
        if (!nodes.hasNext()) return false;
        Node next = nodes.next();
        SkriptLogger.setNode(thisNode);

        if (thisNode != next) {
            Skript.error("Super constructor call needs to be first statement in the constructor");
            return false;
        }

        try {
            if (parseResult.regexes.isEmpty()) {
                types = Collections.emptyList();
            } else {
                types = SuperSignatureParser.parse(parseResult.regexes.get(0).group(), SkriptUtil.getCurrentScript(getParser()));
                if (types.contains(null)) throw new NullPointerException();
            }
        } catch (Exception exception) {
            Skript.error("Invalid parameter types for the super constructor");
            return false;
        }
        values = expressions[0] != null ? LiteralUtils.defendExpression(expressions[0]) : null;

        return true;
    }

    public List<Type> getTypes() {
        return types;
    }

    public @Nullable Expression<Object> getValues() {
        return values;
    }

}
