package me.pesekjak.hippo.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.bukkit.NewClassEvent;
import me.pesekjak.hippo.bukkit.StaticBlockCallEvent;
import me.pesekjak.hippo.core.*;
import me.pesekjak.hippo.core.skript.StaticBlockWrapper;
import me.pesekjak.hippo.core.skript.Storage;
import me.pesekjak.hippo.elements.ClassElement;
import me.pesekjak.hippo.elements.structures.StructNewClass;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Name("Static Block")
@Description("Adds new static block to the custom class.")
@Examples({
        "public class Blob:",
        "\tstatic:",
        "\t\tbroadcast \"Hello Blob!\""
})
@Since("1.0.0")
@ClassElement
public class SecStatic extends Section {

    static {
        Skript.registerSection(
                SecStatic.class,
                "static"
        );
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult,
                        @NotNull SectionNode sectionNode,
                        @NotNull List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(NewClassEvent.class)) return false;
        if (!(getParser().getCurrentStructure() instanceof StructNewClass structure)) return false;

        int nextAnnotations = structure.getNextAnnotations().size();
        if (nextAnnotations != 0) {
            Skript.warning("You annotated static block with " + nextAnnotations + " annotations but static blocks cannot be annotated");
            structure.resetNextAnnotations();
        }

        try {
            AbstractClass source = structure.getClassWrapper().getWrappedClass();

            String className = source.getName();
            Storage storage = Storage.of(className);

            StaticBlockWrapper staticBlockWrapper = storage.getStaticBlock();
            if (staticBlockWrapper == null) {
                Skript.error("This class cannot have a static block");
                return false;
            }

            Trigger trigger = loadCode(sectionNode, "static block", StaticBlockCallEvent.class);

            staticBlockWrapper.addStaticTrigger(trigger);
        } catch (Exception exception) {
            if (exception.getMessage() != null) Skript.error(exception.getMessage());
            return false;
        }

        return true;
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        return null;
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "static block";
    }

}
