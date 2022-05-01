package me.pesekjak.hippo.skript.classes.syntax.contents.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.events.classcontents.ConstructorEvent;
import me.pesekjak.hippo.utils.events.classcontents.constructors.PostInitEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SecPostInit extends Section {

    static {
        Skript.registerSection(SecPostInit.class,
                "postinit"
        );
    }

    private Trigger postInitTrigger;
    // This section is parsed before SecConstructor, to prevent problems,
    // currently parsed post init section has to be built in SecConstructor,
    // after registering constructor is updated.
    public static SecPostInit currentPostInit;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> list) {
        postInitTrigger = loadCode(sectionNode, "init", PostInitEvent.class);
        if (!getParser().isCurrentEvent(ConstructorEvent.class)) return false;
        currentPostInit = this;
        return true;
    }

    protected void build(@NotNull Event event) {
        SkriptClassBuilder.registeringConstructor.setPostInitTrigger(postInitTrigger);
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "postinit";
    }

}
