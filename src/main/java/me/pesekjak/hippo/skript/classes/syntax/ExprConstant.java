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
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("Constant")
@Description("Represents a Literal.")
@Since("1.0-BETA.1")
public class ExprConstant extends SimpleExpression<Constant> {

    static {
        Skript.registerExpression(ExprConstant.class, Constant.class, ExpressionType.COMBINED,
                "\\!(%-number%[(1¦B|2¦S|3¦I|4¦L|5¦F|6¦D)]|%-boolean%|%-string%|%-character%|%-javatype%\\:<([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*>)"
        );
    }

    private Expression<Number> numberExpression;
    private Expression<Boolean> booleanExpression;
    private Expression<String> stringExpression;
    private Expression<Character> characterExpression;
    private Expression<?> typeExpression;
    private int parseMark;

    private String constantPath;

    @Override
    protected Constant @NotNull [] get(@NotNull Event event) {
        Constant constant = null;

        if(numberExpression != null) {
            Number number = numberExpression.getSingle(event);
            switch (parseMark) {
                case 1 -> number = number.byteValue();
                case 2 -> number = number.shortValue();
                case 3 -> number = number.intValue();
                case 4 -> number = number.longValue();
                case 5 -> number = number.floatValue();
                case 6 -> number = number.doubleValue();
            }
            constant = new Constant(number);
        } else if (booleanExpression != null) {
            constant = new Constant(booleanExpression.getSingle(event));
        } else if (stringExpression != null) {
            constant = new Constant(stringExpression.getSingle(event));
        } else if (characterExpression != null) {
            constant = new Constant(characterExpression.getSingle(event));
        } else if (typeExpression != null) {
            Type type = SkriptClassBuilder.getTypeFromExpression(typeExpression);
            if(type == null) return new Constant[0];
            if(constantPath.equals("class")) {
                constant = new Constant(type.toASMType());
            } else {
                constant = new Constant(type, constantPath);
            }
        }

        return new Constant[] { constant };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Constant> getReturnType() {
        return Constant.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "constant";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        numberExpression = SkriptUtils.defendExpression(expressions[0]);
        booleanExpression = SkriptUtils.defendExpression(expressions[1]);
        stringExpression = SkriptUtils.defendExpression(expressions[2]);
        characterExpression = SkriptUtils.defendExpression(expressions[3]);
        typeExpression = SkriptUtils.defendExpression(expressions[4]);
        parseMark = parseResult.mark;
        if(parseResult.regexes.size() > 0) constantPath = parseResult.regexes.get(0).group();
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
