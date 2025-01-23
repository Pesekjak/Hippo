package me.pesekjak.hippo.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.bukkit.NewClassEvent;
import me.pesekjak.hippo.core.AbstractClass;
import me.pesekjak.hippo.core.Field;
import me.pesekjak.hippo.core.NamedParameter;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.core.skript.FieldWrapper;
import me.pesekjak.hippo.core.skript.Storage;
import me.pesekjak.hippo.elements.ClassElement;
import me.pesekjak.hippo.elements.classes.handles.Modifier;
import me.pesekjak.hippo.elements.structures.StructNewClass;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;

@Name("Field")
@Description("Adds new field to the custom class.")
@Examples({
        "public class Blob:",
        "\tpublic int value = 5"
})
@Since("1.0.0")
@ClassElement
@SuppressWarnings("UnstableApiUsage")
public class EffField extends Effect {

    private FieldWrapper fieldWrapper;

    static {
        Hippo.getAddonInstance().syntaxRegistry().register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffField.class)
                        .addPattern("%*modifiers% %*parameter% [= %-object%]")
                        .supplier(EffField::new)
                        .origin(SyntaxOrigin.of(Hippo.getAddonInstance()))
                        .priority(SyntaxInfo.COMBINED)
                        .build()
        );
    }

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "field " + fieldWrapper.field().getName();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        if (!getParser().isCurrentEvent(NewClassEvent.class)) return false;
        if (!(getParser().getCurrentStructure() instanceof StructNewClass structure)) return false;

        List<Annotation> annotations = structure.getNextAnnotations();
        structure.resetNextAnnotations();

        int modifier = 0;
        if (expressions[0] != null)
            for (Object m : expressions[0].getAll(structure.getEvent()))
                modifier |= ((Modifier) m).getValue();

        NamedParameter parameter;
        if (expressions[1] == null) return false;
        parameter = (NamedParameter) expressions[1].getSingle(structure.getEvent());
        if (parameter == null) return false;

        try {
            AbstractClass source = structure.getClassWrapper().getWrappedClass();
            Field field = new Field(source, parameter.name(), parameter.parameter(), modifier, annotations);

            String className = source.getName();
            Storage storage = Storage.of(className);
            if (storage.getTable().containsRow(field.getName())) {
                Skript.error("Class '" + className + "' already has a field with name '" + field.getName() + "'");
                return false;
            }

            source.addField(field);
            fieldWrapper = new FieldWrapper(field, LiteralUtils.defendExpression(expressions[2]));

            storage.getTable().put(field.getName(), field.getDescriptor(), fieldWrapper);

            fieldWrapper.injectCode();
        } catch (Exception exception) {
            if (exception.getMessage() != null) Skript.error(exception.getMessage());
            return false;
        }

        return true;
    }

}
