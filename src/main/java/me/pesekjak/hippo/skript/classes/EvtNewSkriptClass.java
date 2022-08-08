package me.pesekjak.hippo.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.lang.*;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.SkriptLogger;
import com.btk5h.skriptmirror.util.SkriptMirrorUtil;
import com.btk5h.skriptmirror.util.SkriptUtil;
import lombok.Getter;
import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.classes.builder.DynamicClassLoader;
import me.pesekjak.hippo.classes.classtypes.SkriptClass;
import me.pesekjak.hippo.classes.classtypes.SkriptEnum;
import me.pesekjak.hippo.classes.classtypes.SkriptInterface;
import me.pesekjak.hippo.classes.classtypes.SkriptRecord;
import me.pesekjak.hippo.classes.content.Annotation;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.skript.ExprType;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.skript.classes.annotations.SkriptAnnotation;
import me.pesekjak.hippo.skript.classes.annotations.PreScriptLoadListener;
import me.pesekjak.hippo.utils.Reflectness;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class EvtNewSkriptClass extends SelfRegisteringSkriptEvent implements Buildable {

    @Getter
    private ISkriptClass skriptClass;
    private boolean registered = false;

    private Type type;

    private Expression<Modifier> modifierExpression;
    private Expression<ClassType> classTypeExpression;
    private Expression<Object> extendsExpression;
    private Expression<Object> implementsExpression;
    private Expression<Pair> parametersExpression;

    private SectionNode sectionNode;

    private static final Field PARENT_FIELD = Reflectness.getField("parent", Node.class);

    static {
        Skript.registerEvent(
                "Create new Skript Class", EvtNewSkriptClass.class, NewSkriptClassEvent.class,
                "%-modifiers% [skript(-| )]%classtype% <" + SkriptMirrorUtil.PACKAGE + ">[\\([%-pairs%]\\)] [extends %-javatype%] [implements %-javatypes%]"
        );
    }

    @Override
    public void register(@NotNull Trigger trigger) {
        if(!registered) return;
        if(!(skriptClass.getCompileStatus() == ISkriptClass.CompileStatus.AFTER_PARSING))
            return;
        ClassBuilder.forClass(skriptClass).build();
    }

    @Override
    public void unregister(@NotNull Trigger trigger) {
        if(registered) {
            SkriptClassBuilder.CLASS_REGISTRY.remove(skriptClass.getType().dotPath());
            DynamicClassLoader.CLASS_DATA.remove(skriptClass.getType().dotPath());
        }
    }

    @Override
    public void unregisterAll() {
        SkriptClassBuilder.CLASS_REGISTRY.clear();
        DynamicClassLoader.CLASS_DATA.clear();
    }

    @Override
    public boolean init(Literal<?> @NotNull [] literals, int i, SkriptParser.@NotNull ParseResult parseResult) {
        sectionNode = (SectionNode) SkriptLogger.getNode();
        if(sectionNode == null || sectionNode.getKey() == null) return false;
        if (sectionNode.getKey().toLowerCase().startsWith("on ")) return false;

        String name = parseResult.regexes.get(0).group();
        if (SkriptClassBuilder.CLASS_REGISTRY.containsKey(name)) {
            Skript.error("Class '" + name + "' is already registered");
            return false;
        }

        registered = true;

        type = new NonPrimitiveType(name);
        try {
            DynamicClassLoader.getEMPTY_CLASSLOADER().loadClass(name);
            Skript.error("Class '" + name + "' already exists");
            return false;
        } catch (ClassNotFoundException ignored) { }

        modifierExpression = SkriptUtil.defendExpression(parseResult.exprs[0]);
        classTypeExpression = SkriptUtil.defendExpression(parseResult.exprs[1]);
        parametersExpression = SkriptUtil.defendExpression(parseResult.exprs[2]);
        extendsExpression = SkriptUtil.defendExpression(parseResult.exprs[3]);
        implementsExpression = SkriptUtil.defendExpression(parseResult.exprs[4]);

        boolean successful = build(new ScriptEvent());
        if(!successful) {
            SkriptClassBuilder.CLASS_REGISTRY.remove(skriptClass.getType().dotPath());
            DynamicClassLoader.CLASS_DATA.remove(skriptClass.getType().dotPath());
        }
        return successful;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "new skript class " + type.dotPath();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean build(Event event) {
        ClassType classType = null;
        if(classTypeExpression != null)
            classType = classTypeExpression.getSingle(event);
        if(classType == null) {
            Skript.warning("Class " + type.dotPath() + " doesn't have specified class type, defaulting to 'class''");
            classType = ClassType.CLASS;
        }
        skriptClass = SkriptClassBuilder.create(
                switch (classType) {
                    case CLASS -> new SkriptClass(type);
                    case INTERFACE -> new SkriptInterface(type);
                    case ENUM -> new SkriptEnum(type);
                    case ANNOTATION -> new me.pesekjak.hippo.classes.classtypes.SkriptAnnotation(type);
                    case RECORD -> new SkriptRecord(type);
                }
        );
        event = new NewSkriptClassEvent(skriptClass);
        SkriptClassBuilder.ACTIVE_BUILDER.setEvent((NewSkriptClassEvent) event);

        if(parametersExpression != null && classType != ClassType.RECORD) {
            Skript.error("Classes of type '" + classType.name().toLowerCase() + "' can't have defined parameters");
            return false;
        }

        if(modifierExpression != null) {

            Modifier[] modifiers = modifierExpression.getAll(event);
            if(!Arrays.asList(modifiers).contains(Modifier.PUBLIC)) {
                Skript.error("You need to declare public modifier for classes");
                return false;
            }
            Set<Modifier> illegal = Set.of(
                    Modifier.PROTECTED,
                    Modifier.PRIVATE,
                    Modifier.STATIC,
                    Modifier.TRANSIENT,
                    Modifier.SYNCHRONIZED,
                    Modifier.VOLATILE,
                    Modifier.NATIVE,
                    Modifier.DEFAULT
            );
            if(Arrays.stream(modifiers).anyMatch(illegal::contains)) {
                Skript.error("You can't use this combination of modifiers for a class");
                return false;
            }
            if(Modifier.accessConflict(Arrays.asList(modifiers))) {
                Skript.error("You can't declare more than 1 access modifier");
                return false;
            }
            if(Modifier.duplicates(Arrays.asList(modifiers))) {
                Skript.error("You can't declare the same modifier more than once");
                return false;
            }
            if(SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass()
                    .illegalModifiers(ContentType.CLASS, Arrays.asList(modifiers))) {
                Skript.error("You can't use this combination of modifiers for '" + ContentType.CLASS.name().toLowerCase() + "' " +
                        "for class of type '" + classType.name().toLowerCase() + "'");
                return false;
            }

            skriptClass.addModifiers(modifiers);
        }
        if(extendsExpression != null) {
            if (skriptClass.canExtend())
                skriptClass.setSuperClass(ExprType.typeFromObject(extendsExpression.getSingle(event)));
            else {
                Skript.error("Class of type '" + classType.name().toLowerCase() + "' can't extend another class");
                return false;
            }
        }
        if(implementsExpression!= null)
            Arrays.stream(implementsExpression.getAll(event)).forEach(i ->
                    skriptClass.addInterfaces(ExprType.typeFromObject(i)));

        // Setting up annotations
        Map<String, List<Node>> classAnnotations = PreScriptLoadListener.getCLASS_ANNOTATION_MAP().get(getParser().getCurrentScript());
        if(classAnnotations != null) {
            List<Node> toParse = classAnnotations.get(skriptClass.getType().dotPath());
            if (toParse != null) {
                // Class has annotations
                // Setting up parser for parsing the annotations
                getParser().setCurrentEvent("new skript class " + type.dotPath(), NewSkriptClassEvent.class);
                // Storing the old Node to revert the changes once annotations are parsed
                Node thisNode = getParser().getNode();

                for (Node annotationNode : toParse) {

                    // Class annotations don't have parent, meaning they can't be set for the parser,
                    // so we set the parent to this New Skript Class Event.
                    // We need to set the Node for Hippo's debugger system to be accurate.
                    Reflectness.setField(PARENT_FIELD, annotationNode, sectionNode);
                    getParser().setNode(annotationNode);

                    Effect parse = Effect.parse(annotationNode.getKey(), null);
                    if (parse == null) {
                        // If annotations are invalid, error the default message using the annotation node.
                        SkriptLogger.log(
                                new LogEntry(Level.SEVERE, "invalid line - all code has to be put into triggers", annotationNode)
                        );
                        continue;
                    }
                    if (!(parse instanceof SkriptAnnotation.EffStackedAnnotations)) continue;
                    // Add the parsed annotations.
                    skriptClass.addAnnotations(SkriptClassBuilder.ACTIVE_BUILDER.getStackedAnnotations().toArray(new Annotation[0]));
                }
                // Reverts the changes
                getParser().setNode(thisNode);
            }
        }

        // Defining parameters for records
        if(parametersExpression != null && skriptClass instanceof SkriptRecord record)
            for(Pair parameter : parametersExpression.getAll(event)) {
                // Name of each parameter has to be unique
                if(record.getRecordParameters().containsKey(parameter.key())) {
                    Skript.error("Parameter with name '" + parameter.key() + "' is already defined for class '" + skriptClass.getType().dotPath() + "'");
                    return false;
                }
                if(parameter.type().findClass() == void.class) {
                    Skript.error("Parameters can't have void data types");
                    return false;
                }
                record.getRecordParameters().put(parameter.key(), parameter.type());
                me.pesekjak.hippo.classes.content.Field field = new me.pesekjak.hippo.classes.content.Field(parameter.key(), parameter.type());
                field.addModifiers(Modifier.PRIVATE, Modifier.FINAL);
                skriptClass.addContent(field);
            }

        SkriptClassBuilder.ACTIVE_BUILDER.clearStackedAnnotations();
        return true;
    }

}
