package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EffAnnotations extends Effect {

    private Expression<Annotation> annotationExpression;

    static {
        Skript.registerEffect(EffAnnotations.class,
                "annotations: %annotations%"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "annotations";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        annotationExpression = SkriptUtils.defendExpression(expressions[0]);
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        build(SkriptClassBuilder.getCurrentEvent());
        return true;
    }

    protected void build(@NotNull Event event) {
        Annotation[] annotations = annotationExpression.getAll(event);
        SkriptClass registeringClass = SkriptClassBuilder.getRegisteringClass();
        Arrays.stream(annotations).toList().forEach(registeringClass::addAnnotation);
    }
}
