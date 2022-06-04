package me.pesekjak.hippo.skript.classes.syntax.contents.methods;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.contents.Method;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import me.pesekjak.hippo.utils.events.classcontents.MethodCallEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Runnable Method")
@Description("Creates a new Runnable Method for the current class.")
@Since("1.0-BETA.1")
public class SecMethod extends Section {

    static {
        Skript.registerSection(SecMethod.class,
                "%javamodifiers% %pair%\\([%-pairs%]\\) [throws %-javatypes%]"
        );
    }

    private Expression<Modifier> modifierExpression;
    private Expression<Pair> pairExpression;
    private Expression<Pair> argumentExpression;
    private Expression<?> exceptionExpression;
    private Trigger methodTrigger;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> list) {
        modifierExpression = SkriptUtils.defendExpression(expressions[0]);
        pairExpression = SkriptUtils.defendExpression(expressions[1]);
        argumentExpression = SkriptUtils.defendExpression(expressions[2]);
        exceptionExpression = SkriptUtils.defendExpression(expressions[3]);
        methodTrigger = loadCode(sectionNode, "method", MethodCallEvent.class);
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        return build(SkriptClassBuilder.getCurrentEvent());
    }

    protected boolean build(@NotNull Event event) {
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
            List<Type> exceptions = new ArrayList<>();
            for (Object typeObject : exceptionExpression.getAll(event)) {
                if (typeObject instanceof Type) {
                    exceptions.add((Type) typeObject);
                } else {
                    exceptions.add(new Type(SkriptReflectHook.classOfJavaType(typeObject)));
                }
            }
            exceptions.stream().toList().forEach(method::addException);
        }
        if(SkriptClassBuilder.getRegisteringClass().getMethod(pair.getName() + ":" + method.getDescriptor()) != null) {
            Skript.error("Method '" + pair.getName() + "' with descriptor '" + method.getDescriptor() + "' already exists for class '" + SkriptClassBuilder.getRegisteringClass().getClassName() + "'");
            return false;
        }
        ((NewSkriptClassEvent) event).getStackedAnnotations().forEach(method::addAnnotation);
        ((NewSkriptClassEvent) event).clearStackedAnnotations();
        SkriptClassBuilder.getRegisteringClass().addMethod(pair.getName() + ":" + method.getDescriptor(), method);
        return true;
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "new code running method";
    }
}
