package me.pesekjak.hippo.elements.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleEvent;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.bukkit.NewClassEvent;
import me.pesekjak.hippo.core.*;
import me.pesekjak.hippo.core.annotations.Annotation;
import me.pesekjak.hippo.core.classes.Class;
import me.pesekjak.hippo.core.classes.Enum;
import me.pesekjak.hippo.core.classes.Interface;
import me.pesekjak.hippo.core.loader.ClassSignature;
import me.pesekjak.hippo.core.loader.ClassUpdate;
import me.pesekjak.hippo.core.skript.ClassWrapper;
import me.pesekjak.hippo.core.skript.StaticBlockWrapper;
import me.pesekjak.hippo.core.skript.Storage;
import me.pesekjak.hippo.elements.ClassElement;
import me.pesekjak.hippo.elements.classes.handles.Modifier;
import me.pesekjak.hippo.elements.effects.EffAnnotations;
import me.pesekjak.hippo.skript.ScriptInactiveEvent;
import me.pesekjak.hippo.utils.DummyEvent;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.UnlockedTrigger;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.*;

@Name("New Class")
@Description("Creates new java class.")
@Examples({
        "public class Blob:",
        "public interface Elephant implements Animal:"
})
@Since("1.0.0")
public class StructNewClass extends Structure {

    private ClassWrapper classWrapper;
    private NewClassEvent event;

    private final LinkedList<EffAnnotations> nextAnnotations = new LinkedList<>();

    static {
        Skript.registerStructure(
                StructNewClass.class,
                "[%-annotations%] %modifiers% (:class|:interface|:enum) %preimport% "
                        + "[extends %-preimport/javatype%] "
                        + "[implements %-preimports/javatypes%]"
        );
    }

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    @Override
    public boolean init(Literal<?> @NotNull [] args,
                        int matchedPattern,
                        SkriptParser.@NotNull ParseResult parseResult,
                        @NotNull EntryContainer entryContainer) {
        int modifier = 0;
        if (args[1] != null)
            for (Object m : args[1].getAll())
                modifier |= ((Modifier) m).getValue();

        if (args[2] == null) return false;
        Type type = Optional.ofNullable((PreImport) args[2].getSingle()).map(PreImport::type).orElse(null);
        if (type == null) return false;

        Type superClass;
        if (args[3] != null)
            superClass = SkriptUtil.collectTypes(args[3], new DummyEvent()).stream().findFirst().orElse(Type.getType(Object.class));
        else
            superClass = Type.getType(Object.class);

        List<Type> interfaces = new ArrayList<>();
        if (args[4] != null)
            interfaces.addAll(SkriptUtil.collectTypes(args[4], new DummyEvent()));

        List<Annotation> annotations = args[0] == null ? Collections.emptyList() : List.of(((Expression<Annotation>) args[0]).getAll(event));

        AbstractClass clazz;

        try {
            if (parseResult.hasTag("class")) {
                clazz = new Class(null, type, superClass, interfaces, modifier, annotations);
            }

            else if (parseResult.hasTag("interface")) {
                if (args[3] != null) {
                    Skript.error("Interface classes cannot extend other classes");
                    return false;
                }
                clazz = new Interface(null, type, interfaces, modifier, annotations);
            }

            else if (parseResult.hasTag("enum")) {
                if (args[3] != null) {
                    Skript.error("Enum classes cannot extend other classes");
                    return false;
                }
                clazz = new Enum(null, type, interfaces, modifier, annotations);
            }

            else
                throw new IllegalStateException();
        } catch (Exception exception) {
            if (exception.getMessage() != null) Skript.error(exception.getMessage());
            return false;
        }

        clazz.setVersion(Hippo.getInstance().getJavaClassFileVersion());

        if (Storage.contains(clazz.getName())) {
            Skript.error("Class with name '" + clazz.getName() + "' already exists");
            return false;
        }
        classWrapper = new ClassWrapper(clazz, (SectionNode) getParser().getNode());
        Storage.create(classWrapper);

        event = new NewClassEvent(classWrapper);

        StaticBlock staticBlock = clazz.getStaticBlock();
        StaticBlockWrapper staticBlockWrapper = new StaticBlockWrapper(staticBlock);
        Storage.of(classWrapper).getTable().put(staticBlock.getName(), staticBlock.getDescriptor(), staticBlockWrapper);
        staticBlockWrapper.injectCode();

        UnlockedTrigger trigger = SkriptUtil.loadCode(
                getParser(),
                this,
                "class " + clazz.getName(),
                null,
                new SimpleEvent(),
                getEntryContainer().getSource(),
                NewClassEvent.class
        );
        for (TriggerItem item : trigger.getItems()) {
            if (Arrays.stream(item.getClass().getAnnotations()).anyMatch(a -> a.annotationType().equals(ClassElement.class)))
                continue;
            SkriptUtil.warning(classWrapper.getNode(), "Invalid class statement");
        }

        if (clazz instanceof Class) {
            if (clazz.getConstructors().size() == 0 && !superClass.getDescriptor().equals(Type.getDescriptor(Object.class))) {
                Skript.error("The class needs to have at least one defined constructor matching the super constructor");
                Storage.clear(classWrapper);
                return false;
            }
        }

        else if (clazz instanceof Enum) {
            if (clazz.getConstructors().size() == 0) {
                Skript.error("The class needs to have at least one defined constructor");
                Storage.clear(classWrapper);
                return false;
            }
        }

        if (nextAnnotations.size() != 0)
            nextAnnotations.forEach(annotation -> SkriptUtil.warning(annotation.getNode(), "Unused annotations"));

        if (getParser().getCurrentScript().getEvents().stream().noneMatch(listener -> listener instanceof ScriptInactiveEvent))
            getParser().getCurrentScript().registerEvent(new ScriptInactiveEvent()); // takes care of loading classes

        return true;
    }

    @Override
    public boolean load() {
        ClassUpdate.get().add(new ClassSignature(classWrapper.getWrappedClass()));
        return true;
    }

    @Override
    public void unload() {
        Storage.clear(classWrapper);
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "hippo class " + classWrapper.getWrappedClass().getName();
    }

    public ClassWrapper getClassWrapper() {
        return classWrapper;
    }

    public NewClassEvent getEvent() {
        return event;
    }

    public @Unmodifiable List<Annotation> getNextAnnotations() {
        List<Annotation> annotations = new ArrayList<>();
        nextAnnotations.forEach(eff -> annotations.addAll(eff.getAnnotations(event)));
        return Collections.unmodifiableList(annotations);
    }

    public void resetNextAnnotations() {
        nextAnnotations.clear();
    }

    public void addNextAnnotations(EffAnnotations effect) {
        nextAnnotations.add(effect);
    }

}
