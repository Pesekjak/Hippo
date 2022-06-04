package me.pesekjak.hippo.skript.utils.syntax.operators;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class CondLogicalOp extends Condition {

    static {
        Skript.registerCondition(CondLogicalOp.class,
                "<.+> && <.+>",
                "<.+> \\|\\| <.+>",
                "!\\(<.+>\\)"
        );
    }

    private Condition first;
    private Condition second;
    private int pattern;

    @Override
    public boolean check(@NotNull Event event) {
        if(pattern == 0) {
            return first.check(event) && second.check(event);
        } else if(pattern == 1) {
            return first.check(event) || second.check(event);
        } else {
            return !first.check(event);
        }
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "logical operator condition";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pattern = i;
        String firstText = parseResult.regexes.get(0).group(0);
        first = Condition.parse(firstText, "Can't understand this condition: '" + firstText + "'");
        if(pattern != 2) {
            String secondText = parseResult.regexes.get(1).group(0);
            second = Condition.parse(secondText, "Can't understand this condition: '" + secondText + "'");
            return first != null && second != null;
        }
        return first != null;
    }
}
