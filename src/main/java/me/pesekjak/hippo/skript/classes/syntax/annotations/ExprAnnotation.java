package me.pesekjak.hippo.skript.classes.syntax.annotations;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.contents.annotation.Annotation;
import me.pesekjak.hippo.classes.contents.annotation.AnnotationElement;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.ClassBuilder;
import me.pesekjak.hippo.skript.classes.syntax.ExprType;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprAnnotation extends SimpleExpression<Annotation> {

    static {
        Skript.registerExpression(ExprAnnotation.class, Annotation.class, ExpressionType.COMBINED,
                "\\@%-javatype%\\([%-annotationelements%]\\)"
        );
    }

    private Expression<?> typeExpression;
    private Expression<AnnotationElement> elementsExpression;

    @Override
    protected Annotation @NotNull [] get(@NotNull Event event) {
        Annotation annotation = null;
        Type type = ClassBuilder.getTypeFromExpression(typeExpression);
        if(type == null) return new Annotation[0];
        annotation = new Annotation(type);
        if(elementsExpression != null) {
            for(AnnotationElement element : elementsExpression.getAll(event)) {
                annotation.addConstant(element);
            }
        }
        return new Annotation[] { annotation };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Annotation> getReturnType() {
        return Annotation.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "annotation";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        typeExpression = SkriptUtils.defendExpression(expressions[0]);
        elementsExpression = SkriptUtils.defendExpression(expressions[1]);
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
