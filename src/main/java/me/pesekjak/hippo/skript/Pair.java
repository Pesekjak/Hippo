package me.pesekjak.hippo.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.util.SkriptUtil;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public record Pair(String key, Type type) {

    public static final String KEY_PATTERN = "[a-zA-Z0-9_]+";

    static {
        Skript.registerExpression(
                ExprPair.class, Pair.class, ExpressionType.COMBINED,
                "(%-primitivetype/javatype/complextype%) <" + KEY_PATTERN + ">" // Primitive has to be first to keep the priority
        );
    }

    public static class ExprPair extends SimpleExpression<Pair> {

        private Expression<Object> typeExpression;
        private String name;

        @Override
        protected Pair @NotNull [] get(@NotNull Event event) {
            Object typeObject = typeExpression.getSingle(event);
            if(typeObject == null) return new Pair[0];
            Type type;
            if(typeObject instanceof Type) {
                type = (Type) typeObject;
            }
            else if(typeObject instanceof Primitive) {
                type = ExprType.typeFromPrimitive((Primitive) typeObject);
            } else {
                type = ExprType.typeFromObject(typeObject);
            }
            if(type == null) return new Pair[0];
            return new Pair[] {new Pair(name, type)};
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
            return name + " pair";
        }

        @Override
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            if(!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
            name = parseResult.regexes.get(0).group();
            if(!name.matches("^[a-zA-Z].*")) {
                Skript.error("The variable name has to start with a letter");
                return false;
            }
            if(name.length() < 1)
                return false;
            typeExpression = SkriptUtil.defendExpression(expressions[0]);
            return true;
        }

    }

}
