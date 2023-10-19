package me.pesekjak.hippo.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.sections.SecWhile;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.bukkit.ThrowableEvent;
import me.pesekjak.hippo.utils.ReflectConverter;
import me.pesekjak.hippo.utils.SkriptUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Name("Throw an Exception")
@Description("Throws an exception inside of a method of a constructor.")
@Examples({
        "public class Elephant extends Animal:",
        "\t@Override",
        "\tpublic void fly() throws Exception:",
        "\t\tthrow new UnsupportedOperationException()"
})
@Since("1.1")
public class EffThrow extends Effect {

    private Expression<Object> exception;
    private Node node;

    static {
        Skript.registerEffect(
                EffThrow.class,
                "throw %object%"
        );
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {

        Throwable exception = Optional.ofNullable(this.exception.getSingle(event))
                .map(ReflectConverter::unwrapIfNecessary)
                .map(ReflectConverter::nullIfNecessary)
                .map(o -> o instanceof Throwable t ? t : null)
                .orElse(null);

        if (exception == null) {
            SkriptUtil.warning(node, "Provided throwable is not valid, new Error will be provided instead");
            exception = new Error();
        }

        if (event instanceof ThrowableEvent throwableEvent)
            throwableEvent.setThrowable(exception);

        // Exit all loops
        while (parent != null) {
            if (parent instanceof SecLoop) {
                ((SecLoop) parent).exit(event);
            } else if (parent instanceof SecWhile) {
                ((SecWhile) parent).reset();
            }
            parent = parent.getParent();
        }

        return null;
    }

    @Override
    protected void execute(@NotNull Event event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "throw "  + exception.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        node = getParser().getNode();
        if (!getParser().isCurrentEvent(ThrowableEvent.class)) return false;
        exception = LiteralUtils.defendExpression(expressions[0]);

        if (!isDelayed.isFalse()) {
            Skript.warning("Throw statement after a delay is useless, "
                    + "as the calling trigger will resume when the delay starts "
                    + "and won't get the exception");
        }

        return true;
    }

}
