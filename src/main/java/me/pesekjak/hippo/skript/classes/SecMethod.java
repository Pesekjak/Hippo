package me.pesekjak.hippo.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
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
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SecMethod extends Section implements Buildable {

    private Method method;

    private Expression<Modifier> modifierExpression;
    private Expression<Pair> pairExpression;
    private Expression<Pair> argumentExpression;
    private Expression<Object> exceptionExpression;

    private int parseMark;

    private SectionNode section;

    static {
        Skript.registerSection(
                SecMethod.class,
                "%modifiers% %pair%\\([%-pairs%(0¦|1¦\\.\\.\\.)]\\) [throws %-javatypes%]"
        );
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> list) {
        if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        if(expressions.length < 4) return false;
        modifierExpression = SkriptUtil.defendExpression(expressions[0]);
        pairExpression = SkriptUtil.defendExpression(expressions[1]);
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
        return "method " + getMethod(event).getName();
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
        Trigger trigger = loadCode(section, "method trigger", MethodCallEvent.class);
        method.setTrigger(trigger);
        return true;
    }

    protected Method getMethod(Event event) {
        if(method != null) return method;

        Modifier[] modifiers = modifierExpression.getAll(event);
        Set<Modifier> illegal = Set.of(
                Modifier.ABSTRACT,
                Modifier.TRANSIENT,
                Modifier.VOLATILE,
                Modifier.NATIVE
        );
        if(Arrays.stream(modifiers).anyMatch(illegal::contains)) {
            Skript.error("You can't use this combination of modifiers for a method with body");
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

        // Prevents conflicts with constructors.
        //noinspection ConstantConditions
        if(!(pairExpression.getSingle(event) instanceof Pair)) {
            return null;
        }

        Pair pair = pairExpression.getSingle(event);
        if(pair == null) return null;

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

        method = new Method(pair.key(), pair.type());
        method.addModifiers(modifiers);
        method.addArguments(arguments.toArray(new Pair[0]));
        method.addExceptions(exceptions.toArray(new Type[0]));
        method.setVararg(parseMark == 1);
        method.addAnnotations(
                SkriptClassBuilder.ACTIVE_BUILDER.getStackedAnnotations().toArray(new Annotation[0])
        );
        SkriptClassBuilder.ACTIVE_BUILDER.clearStackedAnnotations();
        return method;
    }

}
