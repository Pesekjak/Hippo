package me.pesekjak.hippo.skript.classes.syntax.contents;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Constant;
import me.pesekjak.hippo.classes.ConstantArray;
import me.pesekjak.hippo.classes.Modifier;
import me.pesekjak.hippo.classes.contents.Field;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.skript.classes.syntax.ExprConstant;
import me.pesekjak.hippo.skript.classes.syntax.ExprConstantArray;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EffField extends Effect {

    static {
        Skript.registerEffect(EffField.class,
                "%javamodifiers% %pair% [= %-object%]"
        );
    }

    private Expression<Modifier> modifierExpression;
    private Expression<Pair> pairExpression;
    private Expression<?> valueExpression;
    private Node node;

    @Override
    protected void execute(@NotNull Event event) {
        ((NewSkriptClassEvent) event).setCurrentTriggerItem(this);
        ((NewSkriptClassEvent) event).setCurrentNode(node);
        if(!SkriptClassBuilder.validate(event)) return;
        Pair pair = pairExpression.getSingle(event);
        Field field = new Field(pair.getPrimitiveType(), pair.getType(), pair.getName());
        if(modifierExpression != null) {
            Arrays.stream(modifierExpression.getAll(event)).toList().forEach(field::addModifier);
        }
        if(valueExpression != null) {
            if(valueExpression instanceof ExprConstant) {
                field.setConstant((Constant) valueExpression.getSingle(event));
            } else if(valueExpression instanceof ExprConstantArray) {
                field.setConstantArray((ConstantArray) valueExpression.getSingle(event));
            } else {
                field.setValue(valueExpression);
            }
        }
        SkriptClassBuilder.getStackedAnnotations().forEach(field::addAnnotation);
        SkriptClassBuilder.clearStackedAnnotations();
        SkriptClassBuilder.getRegisteringClass().addField(pair.getName() + ":" + field.getDescriptor(), field);
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
        node = getParser().getNode();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}