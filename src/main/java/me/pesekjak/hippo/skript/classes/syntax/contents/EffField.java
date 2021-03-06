package me.pesekjak.hippo.skript.classes.syntax.contents;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.Constant;
import me.pesekjak.hippo.classes.ConstantArray;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.contents.Field;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.skript.classes.syntax.ExprConstant;
import me.pesekjak.hippo.skript.classes.syntax.ExprConstantArray;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Name("Field")
@Description("Creates a new Field for the current class.")
@Since("1.0-BETA.1")
public class EffField extends Effect {

    static {
        Skript.registerEffect(EffField.class,
                "%javamodifiers% %pair% [= %-object%]"
        );
    }

    private Expression<Modifier> modifierExpression;
    private Expression<Pair> pairExpression;
    private Expression<?> valueExpression;

    @Override
    protected void execute(@NotNull Event event) {
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "new field";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        modifierExpression = SkriptUtils.defendExpression(expressions[0]);
        pairExpression = SkriptUtils.defendExpression(expressions[1]);
        valueExpression = SkriptUtils.defendExpression(expressions[2]);
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        return build(SkriptClassBuilder.getCurrentEvent());
    }

    protected boolean build(@NotNull Event event) {
        Pair pair = pairExpression.getSingle(event);
        Field field = new Field(pair.getPrimitiveType(), pair.getType(), pair.getName());
        if(modifierExpression != null) {
            Arrays.stream(modifierExpression.getAll(event)).toList().forEach(field::addModifier);
        }
        if(SkriptClassBuilder.getRegisteringClass().getClassType() == ClassType.RECORD) {
            if(modifierExpression != null && !(Arrays.stream(modifierExpression.getAll(event)).toList().contains(Modifier.STATIC))) {
                Skript.error("You can't add non-static fields for class '" + ((NewSkriptClassEvent) event).getSkriptClass().getClassName() + "' because they are not supported by " + SkriptClassBuilder.getRegisteringClass().getClassType().getIdentifier());
                return false;
            }
        }
        if(valueExpression != null) {
            if(valueExpression instanceof ExprConstant) {
                field.setConstant((Constant) valueExpression.getSingle(event));
            } else if(valueExpression instanceof ExprConstantArray) {
                field.setConstantArray((ConstantArray) valueExpression.getSingle(event));
            } else if(!SkriptReflectHook.getReflectNullClass().isInstance(valueExpression.getSingle(event))) {
                field.setValue(valueExpression);
            }
        }
        if(SkriptClassBuilder.getRegisteringClass().getField(pair.getName()) != null) {
            Skript.error("Field '" + pair.getName() + "' already exists for class '" + SkriptClassBuilder.getRegisteringClass().getClassName() + "'");
            return false;
        }
        ((NewSkriptClassEvent) event).getStackedAnnotations().forEach(field::addAnnotation);
        ((NewSkriptClassEvent) event).clearStackedAnnotations();
        SkriptClassBuilder.getRegisteringClass().addField(pair.getName(), field);
        return true;
    }
}