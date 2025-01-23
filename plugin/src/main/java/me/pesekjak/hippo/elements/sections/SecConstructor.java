package me.pesekjak.hippo.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.bukkit.ConstructorCallEvent;
import me.pesekjak.hippo.bukkit.NewClassEvent;
import me.pesekjak.hippo.core.*;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.core.classes.Enum;
import me.pesekjak.hippo.core.skript.ConstructorWrapper;
import me.pesekjak.hippo.core.skript.Storage;
import me.pesekjak.hippo.elements.ClassElement;
import me.pesekjak.hippo.elements.classes.handles.Modifier;
import me.pesekjak.hippo.elements.effects.EffSuperConstructorCall;
import me.pesekjak.hippo.elements.structures.StructNewClass;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.UnlockedTrigger;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.*;

@Name("Constructor")
@Description("Adds new constructor to the custom class.")
@Examples({
        "public class Blob:",
        "\tprivate String name",
        "\tpublic Blob(String name):",
        "\t\tsuper[]()",
        "\t\tset this.name to {_name}"
})
@Since("1.0.0")
@ClassElement
@SuppressWarnings("UnstableApiUsage")
public class SecConstructor extends Section {

    static {
        Hippo.getAddonInstance().syntaxRegistry().register(
                SyntaxRegistry.SECTION,
                SyntaxInfo.builder(SecConstructor.class)
                        .addPattern("%*modifiers% %javatype%\\([%-*parameters%(0¦|1¦\\.\\.\\.)]\\) [throws %-javatypes%]")
                        .supplier(SecConstructor::new)
                        .origin(SyntaxOrigin.of(Hippo.getAddonInstance()))
                        .priority(SyntaxInfo.COMBINED)
                        .build()
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

        List<Annotation> annotations = structure.getNextAnnotations();
        structure.resetNextAnnotations();

        int modifier = 0;
        if (expressions[0] != null)
            for (Object m : expressions[0].getAll(structure.getEvent()))
                modifier |= ((Modifier) m).getValue();

        if (parseResult.mark == 1) modifier |= Opcodes.ACC_VARARGS;

        Type type;
        if (expressions[1] == null) return false;
        type = Optional.ofNullable((PreImport) expressions[1].getSingle(structure.getEvent())).map(PreImport::type).orElse(null);
        if (type == null) return false;
        if (!type.getDescriptor().equals(structure.getClassWrapper().getWrappedClass().getType().getDescriptor())) return false;

        NamedParameter[] namedParameters;
        if (expressions[2] == null) namedParameters = new NamedParameter[0];
        else namedParameters = (NamedParameter[]) expressions[2].getAll(structure.getEvent());

        Collection<Parameter> parameters = Arrays.stream(namedParameters).map(NamedParameter::parameter).toList();

        Collection<Type> exceptions = SkriptUtil.collectTypes(expressions[3], structure.getEvent());

        try {
            AbstractClass source = structure.getClassWrapper().getWrappedClass();
            Constructor constructor = new Constructor(source, parameters, modifier, annotations, exceptions);
            int argumentsOffset = 0;

            if (source instanceof Enum) {
                constructor.addParameter(0, new Parameter(Type.getType(String.class), Collections.emptyList()));
                constructor.addParameter(1, new Parameter(Type.INT_TYPE, Collections.emptyList()));
                argumentsOffset += 2;
            }

            String className = source.getName();
            Storage storage = Storage.of(className);
            if (storage.getTable().contains(constructor.getName(), constructor.getDescriptor())) {
                Skript.error("Class '" + className + "' already has a constructor with same arguments");
                return false;
            }

            UnlockedTrigger trigger = SkriptUtil.loadCode(
                    getParser(),
                    new SectionSkriptEvent("constructor", this),
                    "constructor",
                    null,
                    new SimpleEvent(),
                    sectionNode,
                    ConstructorCallEvent.class
            );

            TriggerItem first = trigger.getFirst();

            if (!(first instanceof EffSuperConstructorCall superCall)) {
                Skript.error("Constructors need to call to constructor of super class on the first line");
                return false;
            }

            source.addConstructor(constructor);
            ConstructorWrapper constructorWrapper = new ConstructorWrapper(
                    constructor, List.of(namedParameters), trigger, superCall, argumentsOffset
            );

            storage.getTable().put(constructor.getName(), constructor.getDescriptor(), constructorWrapper);

            constructorWrapper.injectCode();
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
        return "constructor";
    }



}
