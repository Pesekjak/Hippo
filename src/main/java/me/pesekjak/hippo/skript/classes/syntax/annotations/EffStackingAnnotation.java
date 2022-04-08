package me.pesekjak.hippo.skript.classes.syntax.annotations;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
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
    private Node node;

    @Override
    protected void execute(@NotNull Event event) {
        ((NewSkriptClassEvent) event).setCurrentTriggerItem(this);
        ((NewSkriptClassEvent) event).setCurrentNode(node);
        if(!SkriptClassBuilder.validate(event)) return;
        for(Annotation annotation : annotationExpression.getAll(event)) {
            SkriptClassBuilder.addStackingAnnotation(annotation);
        }
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "stacking annotation";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        annotationExpression = SkriptUtils.defendExpression(expressions[0]);
        node = getParser().getNode();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && SkriptUtils.canInit(annotationExpression);
    }
}
