package me.pesekjak.hippo.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprModifier extends SimpleExpression<Modifier> {

    private Modifier modifier;

    static {
        Skript.registerExpression(
                ExprModifier.class, Modifier.class, ExpressionType.SIMPLE,
                "public",
                "private",
                "protected",
                "final",
                "static",
                "abstract",
                "transient",
                "synchronized",
                "volatile",
                "native",
                "strictfp",
                "default");
    }

    @Override
    protected Modifier @NotNull [] get(@NotNull Event event) {
        return new Modifier[] {modifier};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Modifier> getReturnType() {
        return Modifier.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return modifier.name().toLowerCase();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        modifier = switch (i) {
            case 0 -> Modifier.PUBLIC;
            case 1 -> Modifier.PRIVATE;
            case 2 -> Modifier.PROTECTED;
            case 3 -> Modifier.FINAL;
            case 4 -> Modifier.STATIC;
            case 5 -> Modifier.ABSTRACT;
            case 6 -> Modifier.TRANSIENT;
            case 7 -> Modifier.SYNCHRONIZED;
            case 8 -> Modifier.VOLATILE;
            case 9 -> Modifier.NATIVE;
            case 10 -> Modifier.STRICTFP;
            default -> Modifier.DEFAULT;
        };
        return true;
    }
}
