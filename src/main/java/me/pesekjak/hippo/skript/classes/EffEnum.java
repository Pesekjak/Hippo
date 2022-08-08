package me.pesekjak.hippo.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.util.SkriptUtil;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.content.Annotation;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.classes.content.Enum;
import me.pesekjak.hippo.skript.ExprType;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EffEnum extends Effect implements Buildable {

    private Enum enumField;

    private Expression<Object> argumentsExpression;
    private Expression<Object> objectExpression;
    private String name;

    static {
        Skript.registerEffect(
                EffEnum.class,
                "<" + Pair.KEY_PATTERN + ">\\[[%-primitivetypes/javatypes/complextypes%]\\]\\([%-objects%]\\)" // Primitive has to be first to keep the priority
        );
    }

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "field " + getEnum(event).getName();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        argumentsExpression = SkriptUtil.defendExpression(expressions[0]);
        objectExpression = SkriptUtil.defendExpression(expressions[1]);
        name = parseResult.regexes.get(0).group();
        return build(SkriptClassBuilder.ACTIVE_BUILDER.getEvent());
    }

    @Override
    public boolean build(Event event) {
        ISkriptClass skriptClass = SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass();
        if(!skriptClass.canHave(ContentType.ENUM)) {
            Skript.error("Class of type '" + skriptClass.getClassType().name().toLowerCase() + "' " +
                    "can't have any content of type '" + ContentType.ENUM.name().toLowerCase() + "'");
            return false;
        }
        if(getEnum(event) == null) return false; // Creation of Enum failed for some reason.
        if(skriptClass.containsContent(getEnum(event))) {
            Skript.error("Enum with name '" + getEnum(event).getIdentifier() + "' already exists for class '" + skriptClass.getType().dotPath() + "'");
            return false;
        }
        skriptClass.addContent(getEnum(event));
        return true;
    }

    protected Enum getEnum(Event event) {
        if(enumField != null) return enumField;
        Type type = SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass().getType();
        enumField = new Enum(name, type);

        if(argumentsExpression != null) {
            List<Type> arguments = new ArrayList<>();
            for(Object argument : argumentsExpression.getAll(event)) {
                Type argumentType;
                if(argument instanceof Type) {
                    argumentType = (Type) argument;
                }
                else if(argument instanceof Primitive) {
                    argumentType = ExprType.typeFromPrimitive((Primitive) argument);
                } else {
                    argumentType = ExprType.typeFromObject(argument);
                }
                arguments.add(argumentType);
            }
            enumField.addSuperArguments(arguments.toArray(new Type[0]));
        }

        if(objectExpression != null)
            enumField.setSuperValues(objectExpression);

        enumField.addAnnotations(
                SkriptClassBuilder.ACTIVE_BUILDER.getStackedAnnotations().toArray(new Annotation[0])
        );
        SkriptClassBuilder.ACTIVE_BUILDER.clearStackedAnnotations();
        return enumField;
    }

}
