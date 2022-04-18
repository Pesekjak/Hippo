package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.classtypes.*;
import me.pesekjak.hippo.classes.registry.SkriptClassRegistry;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvtNewSkriptClass extends SelfRegisteringSkriptEvent {

    static {
        Skript.registerEvent("Create new Skript Class", EvtNewSkriptClass.class, NewSkriptClassEvent.class,
                "%-javamodifiers% [skript(-| )]%skriptclasstype% <([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*> [extends %-javatype%] [implements %-javatypes%]"
        );
    }

    private Type type;
    private NewSkriptClassEvent registeringEvent;
    private SkriptClass skriptClass;
    private Expression<Modifier> modifierExpression;
    private Expression<ClassType> classTypeExpression;
    private Expression<?> extendsExpression;
    private Expression<?> implementsExpression;

    private boolean isRegistered;

    @Override
    public void register(@NotNull Trigger trigger) {

    }

    @Override
    public void unregister(@NotNull Trigger trigger) {
        if(isRegistered) SkriptClassRegistry.REGISTRY.skriptClassMap.remove(type.getDotPath());
    }

    @Override
    public void unregisterAll() {
        if(isRegistered) SkriptClassRegistry.REGISTRY.skriptClassMap.remove(type.getDotPath());
    }

    @Override
    public boolean init(Literal<?> @NotNull [] literals, int i, SkriptParser.@NotNull ParseResult parseResult) {
        String className = parseResult.regexes.get(0).group();
        if (SkriptClassRegistry.REGISTRY.skriptClassMap.containsKey(className)) {
            Skript.error("Class with name '" + className + "' is already registered");
            return false;
        }

        type = new Type(className);

        modifierExpression = SkriptUtils.defendExpression(parseResult.exprs[0]);
        classTypeExpression = SkriptUtils.defendExpression(parseResult.exprs[1]);
        extendsExpression = SkriptUtils.defendExpression(parseResult.exprs[2]);
        implementsExpression = SkriptUtils.defendExpression(parseResult.exprs[3]);

        ClassType classType = classTypeExpression.getSingle(new NewSkriptClassEvent(null));
        if (classType == null) return false;
        switch (classType) {
            case CLASS -> skriptClass = new TypeClass(type);
            case INTERFACE -> skriptClass = new TypeInterface(type);
            case RECORD -> skriptClass = new TypeRecord(type);
            case ENUM -> skriptClass = new TypeEnum(type);
            case ANNOTATION -> skriptClass = new TypeAnnotation(type);
        }

        registeringEvent = new NewSkriptClassEvent(skriptClass);

        skriptClass.setDefineEvent(registeringEvent);
        SkriptClassBuilder.setRegisteringClass(skriptClass);

        if(modifierExpression != null) {
            Modifier[] modifiers = modifierExpression.getAll(registeringEvent);
            Arrays.stream(modifiers).toList().forEach(skriptClass::addModifier);
        }

        if(extendsExpression != null) {
            List<Type> extending = new ArrayList<>();
            for (Object typeObject : extendsExpression.getAll(registeringEvent)) {
                if (typeObject instanceof Type) {
                    extending.add((Type) typeObject);
                } else {
                    extending.add(new Type(SkriptReflectHook.classOfJavaType(typeObject)));
                }
            }
            extending.stream().toList().forEach(skriptClass::addExtendingType);
        }

        if(implementsExpression != null) {
            List<Type> implementing = new ArrayList<>();
            for (Object typeObject : implementsExpression.getAll(registeringEvent)) {
                if (typeObject instanceof Type) {
                    implementing.add((Type) typeObject);
                } else {
                    implementing.add(new Type(SkriptReflectHook.classOfJavaType(typeObject)));
                }
            }
            implementing.stream().toList().forEach(skriptClass::addImplementingType);
        }

        SkriptClassRegistry.REGISTRY.addSkriptClass(className, skriptClass);
        isRegistered = true;

        return true;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "new skript class";
    }
}
