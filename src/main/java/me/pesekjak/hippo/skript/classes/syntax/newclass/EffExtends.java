package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.skript.classes.ClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EffExtends extends Effect {

    private Expression<Type> typeExpression;
    private Node node;

    static {
        Skript.registerEffect(EffExtends.class,
                "extends: %asmtypes%"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {
        ((NewSkriptClassEvent) event).setCurrentTriggerItem(this);
        ((NewSkriptClassEvent) event).setCurrentNode(node);
        if(!ClassBuilder.validate(event)) return;
        Type[] types = typeExpression.getAll(event);
        SkriptClass registeringClass = ClassBuilder.getRegisteringClass();
        Arrays.stream(types).toList().forEach(registeringClass::addExtendingType);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "extends";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        typeExpression = SkriptUtils.defendExpression(expressions[0]);
        node = getParser().getNode();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && SkriptUtils.canInit(typeExpression);
    }
}
