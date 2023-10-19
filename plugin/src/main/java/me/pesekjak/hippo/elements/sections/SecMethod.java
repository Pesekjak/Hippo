package me.pesekjak.hippo.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.bukkit.MethodCallEvent;
import me.pesekjak.hippo.bukkit.NewClassEvent;
import me.pesekjak.hippo.core.*;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.core.skript.MethodWrapper;
import me.pesekjak.hippo.core.skript.Storage;
import me.pesekjak.hippo.elements.ClassElement;
import me.pesekjak.hippo.elements.classes.handles.Modifier;
import me.pesekjak.hippo.elements.structures.StructNewClass;
import me.pesekjak.hippo.utils.SkriptUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

@Name("Method")
@Description("Adds new method to the custom class.")
@Examples({
        "public class Blob:",
        "\tpublic void run(@Nullable String value):",
        "\t\tbroadcast \"Hello %{_value}%!\""
})
@Since("1.0.0")
@ClassElement
public class SecMethod extends Section {

    private int modifier;
    private Type returnType;
    private MethodWrapper methodWrapper;

    static {
        Skript.registerSection(
                SecMethod.class,
                "%*modifiers% %parameter%\\([%-parameters%(0¦|1¦\\.\\.\\.)]\\) [throws %-javatypes%]"
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

        modifier = 0;
        if (expressions[0] != null)
            for (Object m : expressions[0].getAll(structure.getEvent()))
                modifier |= ((Modifier) m).getValue();

        if (parseResult.mark == 1) modifier |= Opcodes.ACC_VARARGS;

        NamedParameter parameter;
        if (expressions[1] == null) return false;
        parameter = (NamedParameter) expressions[1].getSingle(structure.getEvent());
        if (parameter == null) return false;
        returnType = parameter.parameter().getType();

        NamedParameter[] namedParameters;
        if (expressions[2] == null) namedParameters = new NamedParameter[0];
        else namedParameters = (NamedParameter[]) expressions[2].getAll(structure.getEvent());

        Collection<Parameter> parameters = Arrays.stream(namedParameters).map(NamedParameter::parameter).toList();

        Collection<Type> exceptions = SkriptUtil.collectTypes(expressions[3], structure.getEvent());

        try {
            AbstractClass source = structure.getClassWrapper().getWrappedClass();
            Method method = new Method(source, parameter.name(), parameter.parameter(), parameters, modifier, annotations, exceptions);

            String className = source.getName();
            Storage storage = Storage.of(className);
            if (storage.getTable().contains(method.getName(), method.getDescriptor())) {
                Skript.error("Class '" + className + "' already has a method '" + method.getName() + "' with same arguments");
                return false;
            }

            if (method.isAbstract())
                throw new IllegalModifiersException("Method sections cannot be abstract");

            Trigger trigger = loadCode(sectionNode, "method", MethodCallEvent.class);

            source.addMethod(method);
            methodWrapper = new MethodWrapper(method, List.of(namedParameters), trigger);

            storage.getTable().put(method.getName(), method.getDescriptor(), methodWrapper);

            methodWrapper.injectCode();
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
        return "method " + methodWrapper.method().getName();
    }

    public int getModifier() {
        return modifier;
    }

    public Type getReturnType() {
        return returnType;
    }

}
