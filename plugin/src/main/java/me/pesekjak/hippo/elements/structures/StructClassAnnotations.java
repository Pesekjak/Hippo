package me.pesekjak.hippo.elements.structures;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.core.annotations.Annotation;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Collections;
import java.util.List;

@Name("Class Annotations")
@Description("Adds annotations to custom classes.")
@Examples({
        "@HelloWorld(value = true)",
        "public class Blob:"
})
@Since("1.0.0")
@SuppressWarnings("UnstableApiUsage")
public class StructClassAnnotations extends Structure {

    private List<Annotation> annotations;

    static {
        Hippo.getAddonInstance().syntaxRegistry().register(
                SyntaxRegistry.STRUCTURE,
                SyntaxInfo.Structure.builder(StructClassAnnotations.class)
                        .addPattern("%annotations%")
                        .origin(SyntaxOrigin.of(Hippo.getAddonInstance()))
                        .supplier(StructClassAnnotations::new)
                        .priority(SyntaxInfo.COMBINED)
                        .nodeType(SyntaxInfo.Structure.NodeType.SIMPLE)
                        .build()
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
