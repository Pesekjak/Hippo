package me.pesekjak.hippo.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Name("Abstract Method")
@Description("Adds new abstract method to the custom class.")
@Examples({
        "public abstract class Blob:",
        "\tpublic, abstract void run()"
})
@Since("1.0.0")
@ClassElement
public class EffMethod extends Effect {

    private MethodWrapper methodWrapper;

    static {
        Skript.registerEffect(
                EffField.class,
                "%*modifiers% %parameter%\\([%-parameters%(0¦|1¦\\.\\.\\.)]\\) [throws %-javatypes%]"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "method " + methodWrapper.method().getName();
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

        if (parseResult.mark == 1) modifier |= Opcodes.ACC_VARARGS;

        NamedParameter parameter;
        if (expressions[1] == null) return false;
        parameter = (NamedParameter) expressions[1].getSingle(structure.getEvent());
        if (parameter == null) return false;

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

            if (!method.isAbstract())
                throw new IllegalModifiersException("Method effects need to be abstract");

            source.addMethod(method);
            methodWrapper = new MethodWrapper(method, List.of(namedParameters), null);

            storage.getTable().put(method.getName(), method.getDescriptor(), methodWrapper);

            methodWrapper.injectCode();
        } catch (Exception exception) {
            if (exception.getMessage() != null) Skript.error(exception.getMessage());
            return false;
        }

        return true;
    }

}
