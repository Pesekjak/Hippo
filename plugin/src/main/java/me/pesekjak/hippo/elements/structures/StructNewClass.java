package me.pesekjak.hippo.elements.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
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
import me.pesekjak.hippo.utils.DummyEvent;
import me.pesekjak.hippo.utils.SkriptUtil;
import me.pesekjak.hippo.utils.UnlockedTrigger;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Type;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxOrigin;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.*;

@Name("New Class")
@Description("Creates new java class.")
@Examples({
        "public class Blob:",
        "public interface Elephant implements Animal:"
})
@Since("1.0.0")
@SuppressWarnings("UnstableApiUsage")
public class StructNewClass extends Structure {

    public static final Priority PRIORITY = new Priority(440);

    private ClassWrapper classWrapper;
    private NewClassEvent event;

    private final LinkedList<EffAnnotations> nextAnnotations = new LinkedList<>();

    private Literal<Modifier> modifiers;
    private Literal<PreImport> type;
    private Literal<?> superClass;
    private Literal<?> interfaces;
    private SkriptParser.ParseResult parseResult;
    private EntryContainer entryContainer;

    static {
        Hippo.getAddonInstance().syntaxRegistry().register(
                SyntaxRegistry.STRUCTURE,
                SyntaxInfo.Structure.builder(StructNewClass.class)
                        .addPattern("%modifiers% (:class|:interface|:enum) %preimport% "
                                + "[extends %-preimport/javatype%] "
                                + "[implements %-preimports/javatypes%]")
                        .origin(SyntaxOrigin.of(Hippo.getAddonInstance()))
                        .supplier(StructNewClass::new)
                        .priority(SyntaxInfo.COMBINED)
                        .build()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?> @NotNull [] args,
                        int matchedPattern,
                        SkriptParser.@NotNull ParseResult parseResult,
                        @Nullable EntryContainer entryContainer) {
        if (entryContainer == null) return false;
        modifiers = (Literal<Modifier>) args[0];
        type = (Literal<PreImport>) args[1];
        superClass = args[2];
        interfaces = args[3];
        this.parseResult = parseResult;
        this.entryContainer = entryContainer;
        return true;
    }

    @Override
    public @NotNull Priority getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean preLoad() {
        int modifier = 0;
        if (modifiers != null)
            modifier = Modifier.getModifier(modifiers.getAll());

        if (type == null) return false;
        Type type = Optional.ofNullable(this.type.getSingle()).map(PreImport::type).orElse(null);
        if (type == null) return false;

        Type superClass;
        if (this.superClass != null)
            superClass = SkriptUtil.collectTypes(this.superClass, new DummyEvent()).stream().findFirst().orElse(Type.getType(Object.class));
        else
            superClass = Type.getType(Object.class);

        List<Type> interfaces = new ArrayList<>();
        if (this.interfaces != null)
            interfaces.addAll(SkriptUtil.collectTypes(this.interfaces, new DummyEvent()));

        List<Annotation> annotations = new ArrayList<>();
        List<Structure> structures = getParser().getCurrentScript().getStructures();
        int index = structures.indexOf(this);
        if (index > -1) {
            for (int i = index - 1; i >= 0; i--) {
                Structure next = structures.get(i);
                if (next instanceof StructClassAnnotations classAnnotations) {
                    annotations.addAll(classAnnotations.getAnnotations());
                    continue;
                }
                break;
            }
        }

        AbstractClass clazz;

        try {
            if (parseResult.hasTag("class")) {
                clazz = new Class(null, type, superClass, interfaces, modifier, annotations);
            }

            else if (parseResult.hasTag("interface")) {
                if (this.superClass != null) {
                    Skript.error("Interface classes cannot extend other classes");
                    return false;
                }
                clazz = new Interface(null, type, interfaces, modifier, annotations);
            }

            else if (parseResult.hasTag("enum")) {
                if (this.superClass != null) {
                    Skript.error("Enum classes cannot extend other classes");
                    return false;
                }
                clazz = new Enum(null, type, interfaces, modifier, annotations);
            }

            else {
                throw new IllegalStateException();
            }
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

        return true;
    }

    @Override
    public boolean load() {
        AbstractClass clazz = classWrapper.getWrappedClass();
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
                entryContainer.getSource(),
                NewClassEvent.class
        );
        for (TriggerItem item : trigger.getItems()) {
            if (Arrays.stream(item.getClass().getAnnotations()).anyMatch(a -> a.annotationType().equals(ClassElement.class)))
                continue;
            SkriptUtil.warning(classWrapper.getNode(), "Invalid class statement");
        }

        if (clazz instanceof Class) {
            if (clazz.getConstructors().isEmpty() && !clazz.getSuperClass().getDescriptor().equals(Type.getDescriptor(Object.class))) {
                Skript.error("The class needs to have at least one defined constructor matching the super constructor");
                Storage.clear(classWrapper);
                return false;
            }
        }

        else if (clazz instanceof Enum) {
            if (clazz.getConstructors().isEmpty()) {
                Skript.error("The class needs to have at least one defined constructor");
                Storage.clear(classWrapper);
                return false;
            }
        }

        if (!nextAnnotations.isEmpty())
            nextAnnotations.forEach(annotation -> SkriptUtil.warning(annotation.getNode(), "Unused annotations"));

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
