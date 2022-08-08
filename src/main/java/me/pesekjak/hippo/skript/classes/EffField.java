package me.pesekjak.hippo.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.util.SkriptUtil;
import me.pesekjak.hippo.classes.ISkriptClass;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.content.Annotation;
import me.pesekjak.hippo.classes.content.ContentType;
import me.pesekjak.hippo.classes.content.Field;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

public class EffField extends Effect implements Buildable {

    private Field field;

    private Expression<Modifier> modifierExpression;
    private Expression<Pair> pairExpression;
    private Expression<Object> valueExpression;

    static {
        Skript.registerEffect(
                EffField.class,
                "%modifiers% %pair% [= %-object%]"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "field " + getField(event).getName();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        modifierExpression = SkriptUtil.defendExpression(expressions[0]);
        pairExpression = SkriptUtil.defendExpression(expressions[1]);
        valueExpression = SkriptUtil.defendExpression(expressions[2]);
        return build(SkriptClassBuilder.ACTIVE_BUILDER.getEvent());
    }

    @Override
    public boolean build(Event event) {
        ISkriptClass skriptClass = SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass();
        if(!skriptClass.canHave(ContentType.FIELD)) {
            Skript.error("Class of type '" + skriptClass.getClassType().name().toLowerCase() + "' " +
                    "can't have any content of type '" + ContentType.FIELD.name().toLowerCase() + "'");
            return false;
        }
        if(getField(event) == null) return false; // Creation of Field failed for some reason.
        if(skriptClass.containsContent(getField(event))) {
            Skript.error("Field with name '" + getField(event).getIdentifier() + "' already exists for class '" + skriptClass.getType().dotPath() + "'");
            return false;
        }
        skriptClass.addContent(getField(event));
        return true;
    }

    protected Field getField(Event event) {
        if(field != null) return field;

        Modifier[] modifiers = modifierExpression.getAll(event);
        Set<Modifier> illegal = Set.of(
                Modifier.ABSTRACT,
                Modifier.SYNCHRONIZED,
                Modifier.STRICTFP
        );
        if(Arrays.stream(modifiers).anyMatch(illegal::contains)) {
            Skript.error("You can't use this combination of modifiers for a field");
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
                .illegalModifiers(ContentType.FIELD, Arrays.asList(modifiers))) {
            Skript.error("You can't use this combination of modifiers for '" + ContentType.FIELD.name().toLowerCase() + "' " +
                    "for class of type '" + SkriptClassBuilder.ACTIVE_BUILDER.getSkriptClass().getClassType().name().toLowerCase() + "'");
            return null;
        }

        Pair pair = pairExpression.getSingle(event);
        if(pair == null) return null;

        if(pair.type().findClass() == void.class) {
            Skript.error("Fields can't have void data types");
            return null;
        }

        field = new Field(pair.key(), pair.type(), valueExpression);
        field.addModifiers(modifiers);
        field.addAnnotations(
                SkriptClassBuilder.ACTIVE_BUILDER.getStackedAnnotations().toArray(new Annotation[0])
        );
        SkriptClassBuilder.ACTIVE_BUILDER.clearStackedAnnotations();
        return field;
    }

}
