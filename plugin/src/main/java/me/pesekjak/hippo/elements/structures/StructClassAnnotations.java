package me.pesekjak.hippo.elements.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import me.pesekjak.hippo.core.annotations.Annotation;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.Collections;
import java.util.List;

@Name("Class Annotations")
@Description("Adds annotations to custom classes.")
@Examples({
        "@HelloWorld(value = true)",
        "public class Blob:"
})
@Since("1.0.0")
public class StructClassAnnotations extends Structure {

    private List<Annotation> annotations;

    static {
        Skript.registerSimpleStructure(
                StructClassAnnotations.class,
                "%annotations%"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?> @NotNull [] args,
                        int matchedPattern,
                        SkriptParser.@NotNull ParseResult parseResult,
                        @Nullable EntryContainer entryContainer) {
        annotations = List.of(((Literal<Annotation>) args[0]).getAll());
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "class annotations";
    }

    public @Unmodifiable List<Annotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

}
