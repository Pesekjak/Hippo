package me.pesekjak.hippo.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.sections.SecWhile;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.util.SkriptUtil;
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EffReturn extends Effect {

    private Expression<?> returnExpression;

    static {
        Skript.registerEffect(EffReturn.class,
                "return [%-object%]"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected TriggerItem walk(@NotNull Event e) {
        if(!(e instanceof MethodCallEvent)) return null;
        Object returnObject = null;
        if(returnExpression != null)
            returnObject = returnExpression.getSingle(e);
        ((MethodCallEvent) e).setOutput(returnObject);
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
        return "method return";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if(!getParser().isCurrentEvent(MethodCallEvent.class)) return false;
        returnExpression = SkriptUtil.defendExpression(expressions[0]);
        return true;
    }

}
