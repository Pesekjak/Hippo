package me.pesekjak.hippo.skript.classes.annotations;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.util.SkriptUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.classtypes.SkriptAnnotation;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.classes.types.PrimitiveType;
import me.pesekjak.hippo.skript.ExprType;
import me.pesekjak.hippo.skript.Pair;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import static me.pesekjak.hippo.skript.Pair.KEY_PATTERN;

@AllArgsConstructor
public class DefaultValue {

    @Getter @Setter @NotNull
    private Object value;

    static {
        Skript.registerExpression(
                ExprDefaultValue.class, DefaultValue.class, ExpressionType.COMBINED,
                "%-number%(1¦B|2¦S|3¦I|4¦L|5¦F|6¦D)",
                "%-primitivetype/javatype/complextype%\\:\\:<" + KEY_PATTERN + ">");
    }

    public static boolean canBeDefault(Type type) {
        if(type instanceof PrimitiveType) return true;
        if(SkriptClassBuilder.getSkriptClass(type.dotPath()) instanceof SkriptAnnotation)
            return true;
        Class<?> classObject = type.findClass();
        if(type.isArray()) // We check for non-array classes
            classObject = new NonPrimitiveType(type.dotPath()).findClass();
        if(classObject == null) return false;
        if(classObject.equals(Class.class)) return true;
        if(classObject.equals(String.class)) return true;
        return Enum.class.isAssignableFrom(classObject);
    }

    public static class ExprDefaultValue extends SimpleExpression<DefaultValue> {

        private Expression<?> valueExpression;
        private int parseMark;
        private String path;

        private int pattern;

        private Node node;

        @Override
        protected DefaultValue @NotNull [] get(@NotNull Event event) {
            Object o = valueExpression.getSingle(event);
            if(o == null) return new DefaultValue[0];
            if(pattern == 0) {
                if(!(o instanceof Number number)) return new DefaultValue[0];
                return new DefaultValue[] {new DefaultValue(
                        switch (parseMark) {
                            case 1 -> number.byteValue();
                            case 2 -> number.shortValue();
                            case 4 -> number.longValue();
                            case 5 -> number.floatValue();
                            case 6 -> number.doubleValue();
                            default -> number.intValue();
                        }
                )};
            }
            Type type = ExprType.typeFromObject(o);
            if(type == null) return new DefaultValue[0];
            if(!path.equals("class") && (type instanceof PrimitiveType || type.isArray())) {
                if(type instanceof PrimitiveType primitiveType)
                    //noinspection ConstantConditions
                    Logger.warn("You can reference only 'class' from '" + Primitive.fromClass(primitiveType.findClass()).name().toLowerCase() + "' because it's primitive type: " + node.toString());
                else
                    Logger.warn("You can reference only 'class' from '" + type.descriptor() + "' because it's array type: " + node.toString());
                return new DefaultValue[0];
            }
            if(path.equals("class")) {
                return new DefaultValue[] {new DefaultValue(type.toASM())};
            }
            return new DefaultValue[] {new DefaultValue(new Pair(path, type))};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public @NotNull Class<? extends DefaultValue> getReturnType() {
            return DefaultValue.class;
        }

        @Override
        public @NotNull String toString(Event event, boolean b) {
            return pattern == 0 ? "number value" : "constant reference";
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
            pattern = i;
            valueExpression = SkriptUtil.defendExpression(expressions[0]);
            if(i == 0)
                parseMark = parseResult.mark;
            if(i == 1)
                path = parseResult.regexes.get(0).group();
            node = getParser().getNode();
            return true;
        }

    }

}
