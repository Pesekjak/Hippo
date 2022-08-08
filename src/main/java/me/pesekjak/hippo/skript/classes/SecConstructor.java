package me.pesekjak.hippo.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.util.SkriptReflection;
import com.btk5h.skriptmirror.util.SkriptUtil;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.content.Annotation;
import me.pesekjak.hippo.classes.content.ClassContent;
import me.pesekjak.hippo.classes.content.Constructor;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.skript.ExprType;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.utils.Reflectness;
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;

public class SecConstructor extends Section implements Buildable {

    private Constructor constructor;

    private Expression<Modifier> modifierExpression;
    private Expression<Object> typeExpression;
    private Expression<Pair> argumentExpression;
    private Expression<Object> exceptionExpression;

    private int parseMark;

    private SectionNode section;

    private final static Field NODES_FIELD = Reflectness.getField("nodes", SectionNode.class);

    private final static String SEC_CONSTRUCTOR_PATTERN = "%modifiers% %-javatype%\\([%-pairs%(0¦|1¦\\.\\.\\.)]\\) [throws %-javatypes%]";
    private final static String EFF_SUPER_CALL_PATTERN = "super\\[[%-primitivetypes/javatypes/complextypes%]\\]\\([%-objects%]\\)"; // Primitive has to be first to keep the priority

    static {
        Skript.registerSection(
                SecConstructor.class,
                SEC_CONSTRUCTOR_PATTERN
        );
        Skript.registerEffect(EffSuperCall.class,
                EFF_SUPER_CALL_PATTERN
                );
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> list) {
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;

        if(!sectionNode.iterator().hasNext()) {
            Skript.error("Constructor sections need to have bodies and first line has to call to super");
            return false;
        }

        Node superNode = null;
        if(NODES_FIELD != null) {
            @SuppressWarnings("unchecked")
            ArrayList<Node> nodes = (ArrayList<Node>) Reflectness.getField(NODES_FIELD, sectionNode);
            if(nodes != null) superNode = nodes.get(0);
        }
        if(superNode == null) superNode = sectionNode.iterator().next();
        if(superNode.getKey() == null) return false;
        SkriptParser.ParseResult superParse = SkriptReflection.parse_i(
                new SkriptParser(superNode.getKey(), SkriptParser.ALL_FLAGS, ParseContext.DEFAULT),
                EFF_SUPER_CALL_PATTERN, 0, 0);
        if(superParse == null) {
            Skript.error("First effect of the constructor section has to be call to super");
            return false;
        }

        modifierExpression = SkriptUtil.defendExpression(expressions[0]);
        typeExpression = SkriptUtil.defendExpression(expressions[1]);
        argumentExpression = SkriptUtil.defendExpression(expressions[2]);
        exceptionExpression = SkriptUtil.defendExpression(expressions[3]);
        parseMark = parseResult.mark;
        section = sectionNode;
        return build(SkriptClassBuilder.ACTIVE_BUILDER.getEvent());
    }

    @Override
    protected TriggerItem walk(@NotNull Event event) {
        return null;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "constructor";
    }

    @Override
    public boolean build(Event event) {
        ISkriptClass skriptClass = SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass();
        if(!skriptClass.canHave(ContentType.CONSTRUCTOR)) {
            Skript.error("Class of type '" + skriptClass.getClassType().name().toLowerCase() + "' " +
                    "can't have any content of type '" + ContentType.CONSTRUCTOR.name().toLowerCase() + "'");
            return false;
        }
        if(getConstructor(event) == null) return false; // Creation of Constructor failed for some reason.
        if(skriptClass.containsContent(getConstructor(event))) {
            Skript.error("Constructor with the arguments as '" + getConstructor(event).getIdentifier() + "' already exists for class '" + skriptClass.getType().dotPath() + "'");
            return false;
        }

        skriptClass.addContent(getConstructor(event));
        Trigger trigger = loadCode(section, "constructor trigger", MethodCallEvent.class);
        constructor.setTrigger(trigger);
        return true;
    }

    protected Constructor getConstructor(Event event) {
        if(constructor != null) return constructor;

        Modifier[] modifiers = modifierExpression.getAll(event);
        Set<Modifier> illegal = Set.of(
                Modifier.FINAL,
                Modifier.STATIC,
                Modifier.ABSTRACT,
                Modifier.TRANSIENT,
                Modifier.SYNCHRONIZED,
                Modifier.VOLATILE,
                Modifier.NATIVE,
                Modifier.STRICTFP
        );
        if(Arrays.stream(modifiers).anyMatch(illegal::contains)) {
            Skript.error("You can't use this combination of modifiers for a constructor");
            return null;
        }
        if(Modifier.accessConflict(Arrays.asList(modifiers))) {
            Skript.error("You can't declare more than 1 access modifier");
            return null;
        }
        if(Modifier.duplicates(Arrays.asList(modifiers))) {
            Skript.error("You can't declare the same modifier more than once");
            return null;
        }
        if(SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass()
                .illegalModifiers(ContentType.CONSTRUCTOR, Arrays.asList(modifiers))) {
            Skript.error("You can't use this combination of modifiers for '" + ContentType.CONSTRUCTOR.name().toLowerCase() + "' " +
                    "for class of type '" + SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass().getClassType().name().toLowerCase() + "'");
            return null;
        }

        Type type = ExprType.typeFromObject(typeExpression.getSingle(event));
        if(type == null) return null;

        //noinspection ConstantConditions
        if(!SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass().getType().dotPath().equals(type.dotPath()))
            return null;

        List<Pair> arguments = new ArrayList<>();
        List<Type> exceptions = new ArrayList<>();
        if(argumentExpression != null) {
            Set<String> names = new HashSet<>();
            for(Pair argument : argumentExpression.getAll(event)) {
                if(argument.type().findClass() == void.class) {
                    Skript.error("Arguments can't have void types");
                    return null;
                }
                if(names.contains(argument.key())) {
                    Skript.error("Argument '" + argument.key() + "' is already defined");
                    return null;
                }
                names.add(argument.key());
                arguments.add(argument);
            }
        }
        if(exceptionExpression != null) {
            for(Object javaType : exceptionExpression.getAll(event)) {
                exceptions.add(ExprType.typeFromObject(javaType));
            }
        }

        // Setting up varargs
        if(parseMark == 1) {
            if(arguments.size() == 0)
                return null;
            int i = 0;
            for (Pair argument : arguments) {
                if (arguments.size() != ++i) continue;
                arguments.set(i - 1, new Pair(argument.key(), argument.type().array()));
            }
        }

        constructor = new Constructor();
        constructor.addModifiers(modifiers);
        constructor.addArguments(arguments.toArray(new Pair[0]));
        constructor.addExceptions(exceptions.toArray(new Type[0]));
        constructor.setVararg(parseMark == 1);
        constructor.addAnnotations(
                SkriptClassBuilder.ACTIVE_BUILDER.getStackedAnnotations().toArray(new Annotation[0])
        );
        SkriptClassBuilder.ACTIVE_BUILDER.clearStackedAnnotations();
        return constructor;
    }

    public static class EffSuperCall extends Effect implements Buildable {


        private Expression<Object> argumentsExpression;
        private Expression<Object> objectExpression;
        private final List<Type> arguments = new ArrayList<>();

        @Override
        protected void execute(@NotNull Event event) {

        }

        @Override
        public @NotNull String toString(Event event, boolean b) {
            return "super constructor call";
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            if(!getParser().isCurrentEvent(MethodCallEvent.class)) return false;
            if(getParser().getNode() == null) return false;
            argumentsExpression = SkriptUtil.defendExpression(expressions[0]);
            objectExpression = SkriptUtil.defendExpression(expressions[1]);
            if(getParser().getNode().getParent() == null) return false;
            if(getParser().getCurrentScript() == null) return false;
            String sectionKey = getParser().getNode().getParent().getKey();
            if(sectionKey == null) return false;
            SkriptParser.ParseResult sectionParse = SkriptReflection.parse_i(
                    new SkriptParser(sectionKey, SkriptParser.PARSE_LITERALS, ParseContext.EVENT),
                    SEC_CONSTRUCTOR_PATTERN, 0, 0);
            if(sectionParse == null) {
                Skript.error("You can't call to super outside of constructors!");
                return false;
            }

            return build(SkriptClassBuilder.ACTIVE_BUILDER.getEvent());
        }

        @Override
        public boolean build(Event event) {
            if(argumentsExpression == null ||
                    objectExpression == null)
                return true;
            for(Object argument : argumentsExpression.getAll(event)) {
                Type type;
                if(argument instanceof Type) {
                    type = (Type) argument;
                }
                else if(argument instanceof Primitive) {
                    type = ExprType.typeFromPrimitive((Primitive) argument);
                } else {
                    type = ExprType.typeFromObject(argument);
                }
                if(type.findClass() == void.class) {
                    Skript.error("Arguments can't have void types");
                    return false;
                }
                arguments.add(type);
            }
            ClassContent[] content = SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass()
                    .getClassContent().values().toArray(new ClassContent[0]);
            if(!(content[content.length - 1] instanceof Constructor constructor)) return false;
            constructor.addSuperArguments(arguments.toArray(new Type[0]));
            constructor.setSuperValues(objectExpression);
            return true;
        }
    }

}
