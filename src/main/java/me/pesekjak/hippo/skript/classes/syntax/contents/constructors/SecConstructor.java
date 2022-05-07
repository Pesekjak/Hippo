package me.pesekjak.hippo.skript.classes.syntax.contents.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.contents.Constructor;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import me.pesekjak.hippo.utils.events.classcontents.ConstructorEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecConstructor extends Section {

    static {
        Skript.registerSection(SecConstructor.class,
                "%javamodifiers% <[a-zA-Z0-9]*>\\([%-pairs%]\\) super\\([%-objects%]\\) [throws %-javatypes%]"
        );
    }

    private Expression<Modifier> modifierExpression;
    private Expression<Pair> argumentExpression;
    private Expression<Object> superExpression;
    private Expression<?> exceptionExpression;
    private Trigger constructorTrigger;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> list) {
        modifierExpression = SkriptUtils.defendExpression(expressions[0]);
        argumentExpression = SkriptUtils.defendExpression(expressions[1]);
        superExpression = SkriptUtils.defendExpression(expressions[2]);
        exceptionExpression = SkriptUtils.defendExpression(expressions[3]);
        constructorTrigger = loadCode(sectionNode, "constructor", ConstructorEvent.class);
        String className = SkriptClassBuilder.getRegisteringClass().getType().getSimpleName();
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        if(!parseResult.regexes.get(0).group().equalsIgnoreCase(className)) return false;
        return build(SkriptClassBuilder.getCurrentEvent());
    }

    protected boolean build(@NotNull Event event) {
        Constructor constructor = new Constructor();
        SkriptClassBuilder.registeringConstructor = constructor;
        if(SkriptClassBuilder.getRegisteringClass().getClassType() == ClassType.ENUM) {
            constructor.addArgument(new Argument(new Type(String.class), "E1"));
            constructor.addArgument(new Argument(new PrimitiveType(Primitive.INT), "E2"));
            constructor.addSuperArgument(new Argument(new Type(String.class), "E1"));
            constructor.addSuperArgument(new Argument(new PrimitiveType(Primitive.INT), "E2"));
        }
        if(modifierExpression != null) {
            Arrays.stream(modifierExpression.getAll(event)).toList().forEach(constructor::addModifier);
        }
        if(argumentExpression != null) {
            Arrays.stream(argumentExpression.getAll(event)).toList().forEach((argumentPair) -> constructor.addArgument(argumentPair.asArgument()));
        }
        if(superExpression != null) {
            int i = 0;
            for(Object superArgumentObject : superExpression.getAll(event)) {
                i++;
                if(superArgumentObject instanceof Type) {
                    constructor.addSuperArgument(new Argument((Type) superArgumentObject, "A" + i));
                } else if(superArgumentObject instanceof PrimitiveType) {
                    constructor.addSuperArgument(new Argument((PrimitiveType) superArgumentObject, "A" + i));
                } else if(superArgumentObject instanceof Primitive) {
                    constructor.addSuperArgument(new Argument(new PrimitiveType((Primitive) superArgumentObject), "A" + i));
                } else {
                    constructor.addSuperArgument(new Argument(new Type(SkriptReflectHook.classOfJavaType(superArgumentObject)), "A" + i));
                }
            }
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
            exceptions.stream().toList().forEach(constructor::addException);
        }
        if(SkriptClassBuilder.getRegisteringClass().getConstructor(constructor.getName() + ":" + constructor.getDescriptor()) != null) {
            Skript.error("Constructor with descriptor '" + constructor.getDescriptor() + "' already exists for class '" + SkriptClassBuilder.getRegisteringClass().getClassName() + "'");
            return false;
        }
        // This section is parsed after SecInit and SecPostInit,
        // to prevent problems, currently parsed init and post init sections
        // have to be built here, after registering constructor is updated.
        if(SecInit.currentInit != null) SecInit.currentInit.build(event);
        if(SecPostInit.currentPostInit != null) SecPostInit.currentPostInit.build(event);
        SecInit.currentInit = null;
        SecPostInit.currentPostInit = null;
        ((NewSkriptClassEvent) event).getStackedAnnotations().forEach(constructor::addAnnotation);
        ((NewSkriptClassEvent) event).clearStackedAnnotations();
        SkriptClassBuilder.getRegisteringClass().addConstructor(constructor.getName() + ":" + constructor.getDescriptor(), constructor);
        return true;
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "new constructor";
    }
}
