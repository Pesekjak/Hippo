package me.pesekjak.hippo.skript.classes.syntax.annotations;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Constant;
import me.pesekjak.hippo.classes.ConstantArray;
import me.pesekjak.hippo.classes.contents.annotation.AnnotationElement;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("Annotation Element")
@Description("Refers to element of an Annotation.")
@Since("1.0-BETA.1")
public class ExprAnnotationElement extends SimpleExpression<AnnotationElement> {

    static {
        Skript.registerExpression(ExprAnnotationElement.class, AnnotationElement.class, ExpressionType.COMBINED,
                "<[a-zA-Z0-9]*> = (%-constant%|%-constantarray%)"
        );
    }

    private String name;
    private Expression<Constant> constantExpression;
    private Expression<ConstantArray> constantArrayExpression;

    @Override
    protected AnnotationElement @NotNull [] get(@NotNull Event event) {
        AnnotationElement annotationElement = null;
        if(constantExpression != null) {
            Constant constant = constantExpression.getSingle(event);
            annotationElement = new AnnotationElement(name, constant);
        }
        if(constantArrayExpression != null) {
            ConstantArray constantArray = constantArrayExpression.getSingle(event);
            annotationElement = new AnnotationElement(name, constantArray);
        }
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
        constantArrayExpression = SkriptUtils.defendExpression(expressions[1]);
        name = parseResult.regexes.get(0).group();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
