package me.pesekjak.hippo.skript.classes.syntax.contents.methods;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Constant;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.contents.Method;
import me.pesekjak.hippo.skript.classes.ClassBuilder;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EffMethod extends Effect {

    static {
        Skript.registerEffect(EffMethod.class,
                "%javamodifiers% %pair% \\([%-pairs%]\\) [default %-constant%]"
        );
    }

    private Expression<Modifier> modifierExpression;
    private Expression<Pair> pairExpression;
    private Expression<Pair> argumentExpression;
    private Expression<Constant> constantExpression;
    private Node node;

    @Override
    protected void execute(@NotNull Event event) {
        ((NewSkriptClassEvent) event).setCurrentTriggerItem(this);
        ((NewSkriptClassEvent) event).setCurrentNode(node);
        if(!ClassBuilder.validate(event)) return;
        Pair pair = pairExpression.getSingle(event);
        Method method = new Method(pair.getPrimitiveType(), pair.getType(), pair.getName());
        method.setRunnable(false);
        if(modifierExpression != null) {
            Arrays.stream(modifierExpression.getAll(event)).toList().forEach(method::addModifier);
        }
        if(argumentExpression != null) {
            Arrays.stream(argumentExpression.getAll(event)).toList().forEach((argumentPair) -> method.addArgument(argumentPair.asArgument()));
        }
        if(constantExpression != null) {
            method.setDefaultConstant(constantExpression.getSingle(event));
        }
        ClassBuilder.getStackedAnnotations().forEach(method::addAnnotation);
        ClassBuilder.clearStackedAnnotations();
        ClassBuilder.getRegisteringClass().addMethod(pair.getName(), method);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "new not code running method";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        modifierExpression = SkriptUtils.defendExpression(expressions[0]);
        pairExpression = SkriptUtils.defendExpression(expressions[1]);
        argumentExpression = SkriptUtils.defendExpression(expressions[2]);
        constantExpression = SkriptUtils.defendExpression(expressions[3]);
        node = getParser().getNode();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
