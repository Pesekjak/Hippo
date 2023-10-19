package me.pesekjak.hippo.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.sections.SecWhile;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.bukkit.ConstructorCallEvent;
import me.pesekjak.hippo.bukkit.MethodCallEvent;
import me.pesekjak.hippo.bukkit.StaticBlockCallEvent;
import me.pesekjak.hippo.elements.sections.SecConstructor;
import me.pesekjak.hippo.elements.sections.SecMethod;
import me.pesekjak.hippo.elements.sections.SecStatic;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

@Name("Return")
@Description("Returns value from a method or stops the execution.")
@Examples({
        "public class Elephant extends Animal:",
        "\t@Override",
        "\tpublic String name():",
        "\t\treturn \"elephant\""
})
@Since("1.0.0")
public class EffReturn extends Effect {

    private Expression<Object> value;

    static {
        Skript.registerEffect(
                EffReturn.class,
                "return [%-object%]"
        );
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        if (event instanceof MethodCallEvent methodCall && value != null)
            methodCall.setReturned(value.getSingle(event));

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
        if (value == null) return "return";
        return "return "  + value.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        if (!getParser().isCurrentEvent(MethodCallEvent.class, ConstructorCallEvent.class, StaticBlockCallEvent.class)) return false;
        if (!(getParser().getCurrentStructure() instanceof SectionSkriptEvent event)) return false;

        value = expressions[0] != null ? LiteralUtils.defendExpression(expressions[0]) : null;

        if (event.getSection() instanceof SecConstructor && value != null) {
            Skript.warning("Values cannot be returned in constructors");
        }

        else if (event.getSection() instanceof SecMethod methodSection) {
            if (methodSection.getReturnType().getDescriptor().equals(Type.VOID_TYPE.getDescriptor())) {
                if (value != null)
                    Skript.warning("Cannot return a value from a method with void type");
            } else if (value == null) {
                Skript.warning("Missing return value");
            }
        }

        else if (event.getSection() instanceof SecStatic && value != null) {
            Skript.warning("Values cannot be returned in static blocks");
        }

        if (!isDelayed.isFalse()) {
            Skript.warning("Return statement after a delay is useless, "
                    + "as the calling trigger will resume when the delay starts "
                    + "and won't get any returned value");
        }

        return true;
    }

}
