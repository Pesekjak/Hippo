package me.pesekjak.hippo.skript.classes.syntax;

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

public class ExprJavaModifier extends SimpleExpression<Modifier> {

    static {
        Skript.registerExpression(ExprJavaModifier.class, Modifier.class, ExpressionType.SIMPLE,
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
                "strictfp"
        );
    }

    private int pattern;

    @Override
    protected Modifier @NotNull [] get(@NotNull Event event) {
        Modifier modifier = null;
        switch (pattern) {
            case 0 -> modifier = Modifier.PUBLIC;
            case 1 -> modifier = Modifier.PRIVATE;
            case 2 -> modifier = Modifier.PROTECTED;
            case 3 -> modifier = Modifier.FINAL;
            case 4 -> modifier = Modifier.STATIC;
            case 5 -> modifier = Modifier.ABSTRACT;
            case 6 -> modifier = Modifier.TRANSIENT;
            case 7 -> modifier = Modifier.SYNCHRONIZED;
            case 8 -> modifier = Modifier.VOLATILE;
            case 9 -> modifier = Modifier.NATIVE;
            case 10 -> modifier = Modifier.STRICTFP;
        }
        return new Modifier[] { modifier };
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
        return "java modifier";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pattern = i;
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
