package me.pesekjak.hippo.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import me.pesekjak.hippo.utils.events.StaticEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SecStatic extends Section implements Buildable {

    private SectionNode section;

    static {
        Skript.registerSection(
                SecStatic.class,
                "static"
        );
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> list) {
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        section = sectionNode;
        return build(SkriptClassBuilder.ACTIVE_BUILDER.getEvent());
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        return null;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "static section";
    }

    @Override
    public boolean build(Event event) {
        ISkriptClass skriptClass = SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass();
        if(!skriptClass.canHave(ContentType.STATIC_BLOCK)) {
            Skript.error("Class of type '" + skriptClass.getClassType().name().toLowerCase() + "' " +
                    "can't have any content of type '" + ContentType.STATIC_BLOCK.name().toLowerCase() + "'");
            return false;
        }
        if(skriptClass.getStaticTrigger() != null) {
            Skript.error("Class '" + skriptClass.getType().dotPath() + "' already has a static block");
            return false;
        }
        Trigger trigger = loadCode(section, "static trigger", StaticEvent.class);
        skriptClass.setStaticTrigger(trigger);
        return true;
    }

}
