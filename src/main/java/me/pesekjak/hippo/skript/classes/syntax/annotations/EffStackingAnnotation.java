package me.pesekjak.hippo.skript.classes.syntax.annotations;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EffStackingAnnotation extends Effect {

    static {
        Skript.registerEffect(EffStackingAnnotation.class,
                "%annotations%"
        );
    }

    private Expression<Annotation> annotationExpression;

    @Override
    protected void execute(@NotNull Event event) {
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "stacking annotation";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        annotationExpression = SkriptUtils.defendExpression(expressions[0]);
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        build(SkriptClassBuilder.getCurrentEvent());
        return true;
    }

    protected void build(@NotNull Event event) {
        for(Annotation annotation : annotationExpression.getAll(event)) {
            ((NewSkriptClassEvent) event).addStackingAnnotation(annotation);
        }
    }
}
