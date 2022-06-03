package me.pesekjak.hippo.skript.utils.syntax.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EffCompile extends Effect {

    static {
        Skript.registerEffect(EffCompile.class,
                "compile during pars(e time|ing)",
                "compile now"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "compile class during runtime";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        ClassBuilder.forClass(SkriptClassBuilder.getRegisteringClass());
        return true;
    }
}
