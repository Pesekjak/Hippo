package me.pesekjak.hippo.skript.characters.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprCharacter extends SimpleExpression<Character> {

    static {
        Skript.registerExpression(ExprCharacter.class, Character.class, ExpressionType.COMBINED,
                "\\'<^.{1}$>\\'"
        );
    }

    private String character;

    @Override
    protected Character @NotNull [] get(@NotNull Event event) {
        return new Character[] { character.charAt(0) };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Character> getReturnType() {
        return Character.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "character";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        character = parseResult.regexes.get(0).group();
        return true;
    }
}
