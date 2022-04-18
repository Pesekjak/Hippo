package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.classtypes.TypeRecord;;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
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

    @Override
    protected void execute(@NotNull Event event) {
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "record";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pairExpression = SkriptUtils.defendExpression(expressions[0]);
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        build(SkriptClassBuilder.getCurrentEvent());
        return true;
    }

    protected void build(@NotNull Event event) {
        SkriptClass skriptClass = ((NewSkriptClassEvent) event).getSkriptClass();
        if(!(skriptClass instanceof TypeRecord)) {
            Skript.error("You can't set record property for class '" + ((NewSkriptClassEvent) event).getSkriptClass().getClassName() + "' because type of the class isn't record");
            return;
        }
        for(Pair pair : pairExpression.getAll(event)) {
            ((TypeRecord) skriptClass).addRecordConstructorArgument(pair.asArgument());
        }
    }
}
