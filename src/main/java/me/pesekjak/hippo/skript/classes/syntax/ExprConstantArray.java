package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.Constant;
import me.pesekjak.hippo.classes.ConstantArray;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Name("Constant Array")
@Description("Groups Constants into an special array.")
@Since("1.0-BETA.1")
public class ExprConstantArray extends SimpleExpression<ConstantArray> {

    static {
        Skript.registerExpression(ExprConstantArray.class, ConstantArray.class, ExpressionType.COMBINED,
                "\\!\\[%-constants%\\]"
        );
    }

    private Expression<Constant> constantExpression;

    @Override
    protected ConstantArray @NotNull [] get(@NotNull Event event) {
        if(constantExpression == null) return new ConstantArray[0];
        ConstantArray constantArray = new ConstantArray();
        Arrays.stream(constantExpression.getAll(event)).forEach(constantArray::addConstant);
        return new ConstantArray[] { constantArray };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends ConstantArray> getReturnType() {
        return ConstantArray.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "constant array";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        constantExpression = SkriptUtils.defendExpression(expressions[0]);
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
