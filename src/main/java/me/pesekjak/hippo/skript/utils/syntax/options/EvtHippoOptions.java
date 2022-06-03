package me.pesekjak.hippo.skript.utils.syntax.options;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import me.pesekjak.hippo.utils.events.HippoOptionsEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EvtHippoOptions extends SkriptEvent {

    static {
        Skript.registerEvent("Hippo Options", EvtHippoOptions.class, HippoOptionsEvent.class,
                "hippo options"
        );
    }

    @Override
    public boolean init(Literal<?> @NotNull [] literals, int i, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(@NotNull Event event) {
        return false;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "hippo options";
    }
}
