package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.classtypes.TypeRecord;
import me.pesekjak.hippo.classes.registry.SkriptClassRegistry;
import me.pesekjak.hippo.skript.classes.ClassBuilder;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EffRecord extends Effect {

    static {
        Skript.registerEffect(EffRecord.class,
                "record: %pairs%"
        );
    }

    private Expression<Pair> pairExpression;
    private Node node;

    @Override
    protected void execute(@NotNull Event event) {
        ((NewSkriptClassEvent) event).setCurrentTriggerItem(this);
        ((NewSkriptClassEvent) event).setCurrentNode(node);
        if(!ClassBuilder.validate(event)) return;
        SkriptClass skriptClass = SkriptClassRegistry.REGISTRY.getSkriptClass(((NewSkriptClassEvent) event).getClassName());
        if(!(skriptClass instanceof TypeRecord)) {
            Logger.severe("You can't set record property for class '" + ((NewSkriptClassEvent) event).getClassName() + "' because type of the class isn't record: " + ((NewSkriptClassEvent) event).getCurrentNode().toString());
            return;
        }
        for(Pair pair : pairExpression.getAll(event)) {
            ((TypeRecord) skriptClass).addConstructorArgument(pair.asArgument());
        }
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "record";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pairExpression = SkriptUtils.defendExpression(expressions[0]);
        node = getParser().getNode();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && SkriptUtils.canInit(pairExpression);
    }
}
