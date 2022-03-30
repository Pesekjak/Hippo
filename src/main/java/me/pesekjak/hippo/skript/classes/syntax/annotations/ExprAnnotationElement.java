package me.pesekjak.hippo.skript.classes.syntax.annotations;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Constant;
import me.pesekjak.hippo.classes.contents.annotation.AnnotationElement;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprAnnotationElement extends SimpleExpression<AnnotationElement> {

    static {
        Skript.registerExpression(ExprAnnotationElement.class, AnnotationElement.class, ExpressionType.COMBINED,
                "<[a-zA-Z0-9]*> = %-constant%"
        );
    }

    private String name;
    private Expression<Constant> constantExpression;

    @Override
    protected AnnotationElement @NotNull [] get(@NotNull Event event) {
        Constant constant = constantExpression.getSingle(event);
        AnnotationElement annotationElement = new AnnotationElement(name, constant);
        return new AnnotationElement[] { annotationElement };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends AnnotationElement> getReturnType() {
        return AnnotationElement.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "annotation element";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        constantExpression = SkriptUtils.defendExpression(expressions[0]);
        name = parseResult.regexes.get(0).group();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
