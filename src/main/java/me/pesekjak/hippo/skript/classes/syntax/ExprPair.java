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
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.PrimitiveType;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.skript.classes.Pair;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("Pair")
@Description("Pairs DataType and String together.")
@Since("1.0-BETA.1")
public class ExprPair extends SimpleExpression<Pair> {

    static {
        Skript.registerExpression(ExprPair.class, Pair.class, ExpressionType.COMBINED,
                "(%-primitive%|%-primitivetype%|%-javatype%|%-asmtype%) <[a-zA-Z0-9]*>"
        );
    }

    private Expression<Primitive> primitiveExpression;
    private Expression<PrimitiveType> primitiveTypeExpression;
    private Expression<?> javaTypeExpression;
    private Expression<Type> typeExpression;
    private String name;

    @Override
    protected Pair @NotNull [] get(@NotNull Event event) {
        Type type = null;
        if(javaTypeExpression != null) type = SkriptClassBuilder.getTypeFromExpression(javaTypeExpression);
        if(typeExpression != null) type = typeExpression.getSingle(event);
        PrimitiveType primitive = new PrimitiveType(Primitive.NONE);
        if(primitiveTypeExpression != null) primitive = primitiveTypeExpression.getSingle(event);
        if(primitiveExpression != null) primitive = new PrimitiveType(primitiveExpression.getSingle(event));
        return new Pair[] { new Pair(primitive, type, name) };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Pair> getReturnType() {
        return Pair.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "pair";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        primitiveExpression = SkriptUtils.defendExpression(expressions[0]);
        primitiveTypeExpression = SkriptUtils.defendExpression(expressions[1]);
        javaTypeExpression = SkriptUtils.defendExpression(expressions[2]);
        typeExpression = SkriptUtils.defendExpression(expressions[3]);
        name = parseResult.regexes.get(0).group();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
