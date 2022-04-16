package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.sections.SecWhile;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.classcontents.MethodCallEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EffReturn extends Effect {

    static {
        Skript.registerEffect(EffReturn.class,
                "return %-object%"
        );
    }

    private Expression<?> returnExpression;

    @Override
    protected void execute(@NotNull Event event) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected TriggerItem walk(@NotNull Event e) {
        if(!(e instanceof MethodCallEvent)) return null;
        ((MethodCallEvent) e).setOutput(returnExpression.getSingle(e));
        TriggerSection parent = getParent();
        while (parent != null) {
            if (parent instanceof SecLoop) {
                ((SecLoop) parent).exit(e);
            } else if (parent instanceof SecWhile) {
                ((SecWhile) parent).reset();
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "return";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        returnExpression = SkriptUtils.defendExpression(expressions[0]);
        return (getParser().isCurrentEvent(MethodCallEvent.class));
    }
}
