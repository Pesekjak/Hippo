package me.pesekjak.hippo.elements.effects;

import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.bukkit.NewClassEvent;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.elements.ClassElement;
import me.pesekjak.hippo.elements.structures.StructNewClass;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Name("Annotations")
@Description("Adds annotations to the next class element.")
@Examples({
        "public class Elephant extends Animal:",
        "\t@Override, @NotNull",
        "\tpublic int[] values():",
        "\t\treturn new int[0]"
})
@Since("1.0.0")
@ClassElement
@SuppressWarnings("UnstableApiUsage")
public class EffAnnotations extends Effect {

    private Expression<?> annotations;
    private Node node;

    static {
        Hippo.getAddonInstance().syntaxRegistry().register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffAnnotations.class)
                        .addPattern("%*annotations%")
                        .supplier(EffAnnotations::new)
                        .origin(SyntaxOrigin.of(Hippo.getAddonInstance()))
                        .priority(SyntaxInfo.COMBINED)
                        .build()
        );
    }

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "annotations";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        if (!getParser().isCurrentEvent(NewClassEvent.class)) return false;
        if (!(getParser().getCurrentStructure() instanceof StructNewClass structure)) return false;

        annotations = expressions[0];
        node = getParser().getNode();
        structure.addNextAnnotations(this);

        return true;
    }

    public List<Annotation> getAnnotations(Event event) {
        return Arrays.stream(annotations.getAll(event))
                .map(o -> o instanceof Annotation annotation ? annotation : null)
                .filter(Objects::nonNull)
                .toList();
    }

    public Node getNode() {
        return node;
    }

}
