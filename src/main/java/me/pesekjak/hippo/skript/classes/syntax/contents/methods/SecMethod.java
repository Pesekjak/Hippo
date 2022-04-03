package me.pesekjak.hippo.skript.classes.syntax.contents.methods;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.contents.Method;
import me.pesekjak.hippo.skript.classes.ClassBuilder;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import me.pesekjak.hippo.utils.events.classcontents.MethodCallEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SecMethod extends Section {

    static {
        Skript.registerSection(SecMethod.class,
                "%javamodifiers% %pair% \\([%-pairs%]\\) [throws %-asmtypes%]"
        );
    }

    private Expression<Modifier> modifierExpression;
    private Expression<Pair> pairExpression;
    private Expression<Pair> argumentExpression;
    private Expression<Type> exceptionExpression;
    private Node node;
    private Trigger methodTrigger;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> list) {
        modifierExpression = SkriptUtils.defendExpression(expressions[0]);
        pairExpression = SkriptUtils.defendExpression(expressions[1]);
        argumentExpression = SkriptUtils.defendExpression(expressions[2]);
        exceptionExpression = SkriptUtils.defendExpression(expressions[3]);
        node = getParser().getNode();
        methodTrigger = loadCode(sectionNode, "method", MethodCallEvent.class);
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        ((NewSkriptClassEvent) event).setCurrentTriggerItem(this);
        ((NewSkriptClassEvent) event).setCurrentNode(node);
        if(!ClassBuilder.validate(event)) return null;
        Pair pair = pairExpression.getSingle(event);
        Method method = new Method(pair.getPrimitiveType(), pair.getType(), pair.getName());
        method.setRunnable(true);
        method.setTrigger(methodTrigger);
        if(modifierExpression != null) {
            Arrays.stream(modifierExpression.getAll(event)).toList().forEach(method::addModifier);
        }
        if(argumentExpression != null) {
            Arrays.stream(argumentExpression.getAll(event)).toList().forEach((argumentPair) -> method.addArgument(argumentPair.asArgument()));
        }
        if(exceptionExpression != null) {
            Arrays.stream(exceptionExpression.getAll(event)).toList().forEach(method::addException);
        }
        ClassBuilder.getStackedAnnotations().forEach(method::addAnnotation);
        ClassBuilder.clearStackedAnnotations();
        ClassBuilder.getRegisteringClass().addMethod(pair.getName(), method);
        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "new method";
    }
}
