package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.skript.classes.ClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EffSkriptClassModifiers extends Effect {

    static {
        Skript.registerEffect(EffSkriptClassModifiers.class,
                "modifiers: %javamodifiers%"
        );
    }

    private Expression<Modifier> modifierExpression;
    private Node node;

    @Override
    protected void execute(@NotNull Event event) {
        ((NewSkriptClassEvent) event).setCurrentTriggerItem(this);
        ((NewSkriptClassEvent) event).setCurrentNode(node);
        if(!ClassBuilder.validate(event)) return;
        Modifier[] modifiers = modifierExpression.getAll(event);
        SkriptClass registeringClass = ClassBuilder.getRegisteringClass();
        Arrays.stream(modifiers).toList().forEach(registeringClass::addModifier);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "skript class modifiers";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        modifierExpression = SkriptUtils.defendExpression(expressions[0]);
        node = getParser().getNode();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && SkriptUtils.canInit(modifierExpression);
    }
}
