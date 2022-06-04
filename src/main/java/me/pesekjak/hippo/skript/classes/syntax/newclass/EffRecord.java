package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.classtypes.TypeRecord;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("Record Components")
@Description("Sets Record Components for a current record class.")
@Since("1.0-BETA.1")
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
        return build(SkriptClassBuilder.getCurrentEvent());
    }

    protected boolean build(@NotNull Event event) {
        SkriptClass skriptClass = ((NewSkriptClassEvent) event).getSkriptClass();
        if(!(skriptClass instanceof TypeRecord)) {
            Skript.error("You can't set record components for class '" + ((NewSkriptClassEvent) event).getSkriptClass().getClassName() + "' because they are not supported by " + SkriptClassBuilder.getRegisteringClass().getClassType().getIdentifier());
            return false;
        }
        for(Pair pair : pairExpression.getAll(event)) {
            ((TypeRecord) skriptClass).addRecordConstructorArgument(pair.asArgument());
        }
        return true;
    }
}
