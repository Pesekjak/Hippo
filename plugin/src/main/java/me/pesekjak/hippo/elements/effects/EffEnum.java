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
import me.pesekjak.hippo.bukkit.EnumValuesEvent;
import me.pesekjak.hippo.bukkit.NewClassEvent;
import me.pesekjak.hippo.core.AbstractClass;
import me.pesekjak.hippo.core.Field;
import me.pesekjak.hippo.core.Parameter;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.core.classes.Enum;
import me.pesekjak.hippo.core.skript.EnumWrapper;
import me.pesekjak.hippo.core.skript.Storage;
import me.pesekjak.hippo.elements.ClassElement;
import me.pesekjak.hippo.elements.SyntaxCommons;
import me.pesekjak.hippo.elements.structures.StructNewClass;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.parser.SuperSignatureParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Collections;
import java.util.List;

@Name("Enum")
@Description("Adds new enum constant to the custom enum class.")
@Examples({
        "public enum Blob:",
        "\tpublic int value",
        "\tBLOB[int](1)",
        "\tFOO[int](10)",
        "\tpublic Blob(int value):",
        "\t\tsuper[]()",
        "\t\tset this.value to {_value}"
})
@Since("1.0.0")
@ClassElement
@SuppressWarnings("UnstableApiUsage")
public class EffEnum extends Effect {

    private EnumWrapper enumWrapper;
    private List<Type> types;
    private Expression<Object> values;

    static {
        Hippo.getAddonInstance().syntaxRegistry().register(
                SyntaxRegistry.EFFECT,
                SyntaxInfo.builder(EffEnum.class)
                        .addPattern("enum <" + SyntaxCommons.VARIABLE_NAME + ">\\[[<.+>]\\]\\([%-objects%]\\)")
                        .supplier(EffEnum::new)
                        .origin(SyntaxOrigin.of(Hippo.getAddonInstance()))
                        .priority(SyntaxInfo.COMBINED)
                        .build()
        );
    }

    @Override
    public void execute(@NotNull Event event) {
        if (!(event instanceof EnumValuesEvent callEvent)) return;
        if (getValues() == null) return;
        callEvent.setArguments(getValues().getAll(event));
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "enum " + enumWrapper.field().getName();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions,
                        int matchedPattern,
                        @NotNull Kleenean isDelayed,
                        SkriptParser.@NotNull ParseResult parseResult) {
        if (!getParser().isCurrentEvent(NewClassEvent.class)) return false;
        if (!(getParser().getCurrentStructure() instanceof StructNewClass structure)) return false;

        if (!(structure.getClassWrapper().getWrappedClass() instanceof Enum)) {
            Skript.error("Only enum classes can contain enum constants");
            return false;
        }

        List<Annotation> annotations = structure.getNextAnnotations();
        structure.resetNextAnnotations();

        int modifier = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM;

        if (parseResult.regexes.isEmpty()) return false;
        String name = parseResult.regexes.get(0).group();

        try {
            if (parseResult.regexes.size() != 2) {
                types = Collections.emptyList();
            } else {
                types = SuperSignatureParser.parse(parseResult.regexes.get(1).group(), SkriptUtil.getCurrentScript(getParser()));
                if (types.contains(null)) throw new NullPointerException();
            }
        } catch (Exception exception) {
            Skript.error("Invalid parameter types for the super constructor");
            return false;
        }

        values = expressions[0] != null ? LiteralUtils.defendExpression(expressions[0]) : null;

        try {
            AbstractClass source = structure.getClassWrapper().getWrappedClass();
            Field field = new Field(source, name, new Parameter(source.getType(), Collections.emptyList()), modifier, annotations);

            String className = source.getName();
            Storage storage = Storage.of(className);
            if (storage.getTable().containsRow(field.getName())) {
                Skript.error("Class '" + className + "' already has a field with name '" + field.getName() + "'");
                return false;
            }

            source.addField(field);
            enumWrapper = new EnumWrapper(field, this);

            storage.getTable().put(field.getName(), field.getDescriptor(), enumWrapper);

            enumWrapper.injectCode();
        } catch (Exception exception) {
            if (exception.getMessage() != null) Skript.error(exception.getMessage());
            return false;
        }

        return true;
    }

    public List<Type> getTypes() {
        return types;
    }

    public @Nullable Expression<Object> getValues() {
        return values;
    }

}
