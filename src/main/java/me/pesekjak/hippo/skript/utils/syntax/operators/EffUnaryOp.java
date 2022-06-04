package me.pesekjak.hippo.skript.utils.syntax.operators;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.utils.SkriptUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Name("Unary Operators")
@Description("Equivalents to Java Unary operators.")
@Since("1.0-BETA.1")
public class EffUnaryOp extends Effect {

    static {
        Skript.registerEffect(EffUnaryOp.class,
                "%number%++",
                "%number%--"
        );
    }

    private Expression<Number> objectExpression;
    private int pattern;

    @Override
    protected void execute(@NotNull Event event) {
        if(pattern == 0) {
            objectExpression.change(event, new Number[]{1}, Changer.ChangeMode.ADD);
        } else {
            objectExpression.change(event, new Number[]{-1}, Changer.ChangeMode.ADD);
        }
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "arithmetic operator";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pattern = i;
        objectExpression = SkriptUtils.defendExpression(expressions[0]);
        Class<?>[] rs = objectExpression.acceptChange(Changer.ChangeMode.ADD);
        if(rs == null) {
            ClassInfo<?> c = Classes.getSuperClassInfo(objectExpression.getReturnType());
            Changer<?> changer = c.getChanger();
            String what = changer == null || !Arrays.equals(changer.acceptChange(Changer.ChangeMode.ADD), rs) ? objectExpression.toString(null, false) : c.getName().withIndefiniteArticle();
            Skript.error(what + " can't have anything " + (pattern == 0 ? "added to" : "removed from") + " it", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }
}
