package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.utils.events.classcontents.MethodCallEvent;
import me.pesekjak.hippo.utils.events.classcontents.constructors.PostInitEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("This")
@Description("Refers to the current object in a Method or Constructor.")
@Since("1.0-BETA.1")
public class ExprThis extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprThis.class, Object.class, ExpressionType.SIMPLE,
                "this"
        );
    }

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        if(event instanceof MethodCallEvent) return new Object[] { ((MethodCallEvent) event).getInstance() };
        if(event instanceof PostInitEvent) return new Object[] { ((PostInitEvent) event).getInstance() };
        return new Object[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "this";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        return getParser().isCurrentEvent(MethodCallEvent.class) || getParser().isCurrentEvent(PostInitEvent.class);
    }
}
