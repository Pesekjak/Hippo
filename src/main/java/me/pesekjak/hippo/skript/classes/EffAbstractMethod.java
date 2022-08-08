package me.pesekjak.hippo.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.util.SkriptUtil;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.content.Annotation;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.classes.content.Method;
import me.pesekjak.hippo.skript.ExprType;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.skript.classes.annotations.DefaultValue;
import me.pesekjak.hippo.skript.classes.annotations.SkriptAnnotation;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class EffAbstractMethod extends Effect implements Buildable {

    private Method method;

    private Expression<Modifier> modifierExpression;
    private Expression<Pair> pairExpression;
    private Expression<Pair> argumentExpression;
    private Expression<Object> exceptionExpression;
    private Expression<?> defaultExpression;

    private int parseMark;
    private int pattern;

    static {
        Skript.registerEffect(
                EffAbstractMethod.class,
                "%modifiers% %pair%\\([%-pairs%(0¦|1¦\\.\\.\\.)]\\) [throws %-javatypes%] [default %-boolean/string/character/annotationvalue%]",
                "%modifiers% %pair%\\([%-pairs%(0¦|1¦\\.\\.\\.)]\\) [throws %-javatypes%] [default \\[%-booleans/strings/characters/annotationvalues%\\]]"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "abstract method " + getMethod(event).getName();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        modifierExpression = SkriptUtil.defendExpression(expressions[0]);
        pairExpression = SkriptUtil.defendExpression(expressions[1]);
        argumentExpression = SkriptUtil.defendExpression(expressions[2]);
        exceptionExpression = SkriptUtil.defendExpression(expressions[3]);
        defaultExpression = SkriptUtil.defendExpression(expressions[4]);
        parseMark = parseResult.mark;
        pattern = i;
        return build(SkriptClassBuilder.ACTIVE_BUILDER.getEvent());
    }

    @Override
    public boolean build(Event event) {
        ISkriptClass skriptClass = SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass();
        if(!skriptClass.canHave(ContentType.METHOD)) {
            Skript.error("Class of type '" + skriptClass.getClassType().name().toLowerCase() + "' " +
                    "can't have any content of type '" + ContentType.METHOD.name().toLowerCase() + "'");
            return false;
        }
        if(getMethod(event) == null) return false; // Creation of Method failed for some reason.
        if(skriptClass.containsContent(getMethod(event))) {
            Skript.error("Method with the same name and arguments as '" + getMethod(event).getIdentifier() + "' already exists for class '" + skriptClass.getType().dotPath() + "'");
            return false;
        }
        skriptClass.addContent(getMethod(event));
        method.setTrigger(null); // has no body
        return true;
    }

    protected Method getMethod(Event event) {
        if(method != null) return method;

        Modifier[] modifiers = modifierExpression.getAll(event);
        if(!Arrays.asList(modifiers).contains(Modifier.ABSTRACT)) {
            Skript.error("You need to declare abstract modifier for methods without body");
            return null;
        }
        Set<Modifier> illegal = Set.of(
                Modifier.FINAL,
                Modifier.STATIC,
                Modifier.TRANSIENT,
                Modifier.SYNCHRONIZED,
                Modifier.VOLATILE,
                Modifier.NATIVE,
                Modifier.STRICTFP
        );
        if(Arrays.stream(modifiers).anyMatch(illegal::contains)) {
            Skript.error("You can't use this combination of modifiers for a abstract method");
            return null;
        }
        if(Arrays.asList(modifiers).contains(Modifier.PRIVATE)) {
            Skript.error("Combination of modifiers 'abstract' and 'private' is illegal");
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
                .illegalModifiers(ContentType.METHOD, Arrays.asList(modifiers))) {
            Skript.error("You can't use this combination of modifiers for '" + ContentType.METHOD.name().toLowerCase() + "' " +
                    "for class of type '" + SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass().getClassType().name().toLowerCase() + "'");
            return null;
        }

        Pair pair = pairExpression.getSingle(event);
        if(pair == null) return null;

        List<Pair> arguments = new ArrayList<>();
        List<Type> exceptions = new ArrayList<>();
        if(argumentExpression != null)
            arguments.addAll(Arrays.asList(argumentExpression.getAll(event)));
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

        SkriptAnnotation.AnnotationElement defaultElement = null;
        if(defaultExpression != null) {
            if(!(SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass() instanceof me.pesekjak.hippo.classes.classtypes.SkriptAnnotation)) {
                Skript.error("You can't specify default value for a abstract method in a class of type '" +
                        SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass().getClassType().name().toLowerCase() + "'");
                return null;
            }
            Object[] values = defaultExpression.getAll(event);
            defaultElement = new SkriptAnnotation.AnnotationElement("default");
            for (Object o : values) {
                if (o instanceof DefaultValue defaultValue)
                    defaultElement.addObjects(defaultValue.getValue());
                else
                    defaultElement.addObjects(o);
            }
            if (pattern == 1) defaultElement.setArray(true);
        }

        method = new Method(pair.key(), pair.type());
        method.addModifiers(modifiers);
        method.addArguments(arguments.toArray(new Pair[0]));
        method.addExceptions(exceptions.toArray(new Type[0]));
        method.setVararg(parseMark == 1);
        method.setDefaultValue(defaultElement);
        method.addAnnotations(
                SkriptClassBuilder.ACTIVE_BUILDER.getStackedAnnotations().toArray(new Annotation[0])
        );
        SkriptClassBuilder.ACTIVE_BUILDER.clearStackedAnnotations();
        return method;
    }

}
