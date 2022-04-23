package me.pesekjak.hippo.skript.classes.syntax.contents;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import me.pesekjak.hippo.utils.events.classcontents.StaticInitializationEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SecStatic extends Section {

    static {
        Skript.registerSection(SecStatic.class,
                "static"
        );
    }

    private Trigger staticTrigger;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> list) {
        staticTrigger = loadCode(sectionNode, "static", StaticInitializationEvent.class);
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        build(SkriptClassBuilder.getCurrentEvent());
        return true;
    }

    protected void build(@NotNull Event event) {
        SkriptClassBuilder.getRegisteringClass().setStaticTrigger(staticTrigger);
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "static initialization";
    }

}
