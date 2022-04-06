package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.ClassBuilder;
import me.pesekjak.hippo.skript.classes.syntax.ExprType;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffImplements extends Effect {

    private Expression<?> typeExpression;
    private Node node;

    static {
        Skript.registerEffect(EffImplements.class,
                "implements: %-javatypes%"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {
        ((NewSkriptClassEvent) event).setCurrentTriggerItem(this);
        ((NewSkriptClassEvent) event).setCurrentNode(node);
        if(!ClassBuilder.validate(event)) return;
        List<Type> types = new ArrayList<>();
        for(Object typeObject : typeExpression.getAll(event)) {
            if(typeObject instanceof Type) {
                types.add((Type) typeObject);
            } else {
                types.add(new Type(SkriptReflectHook.classOfJavaType(typeObject)));
            }
        }
        SkriptClass registeringClass = ClassBuilder.getRegisteringClass();
        types.stream().toList().forEach(registeringClass::addImplementingType);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "implements";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        typeExpression = SkriptUtils.defendExpression(expressions[0]);
        node = getParser().getNode();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && SkriptUtils.canInit(typeExpression);
    }
}
