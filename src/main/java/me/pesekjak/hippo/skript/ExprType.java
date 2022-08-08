package me.pesekjak.hippo.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.btk5h.skriptmirror.JavaType;
import com.btk5h.skriptmirror.util.SkriptUtil;
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.types.NonPrimitiveType;
import me.pesekjak.hippo.classes.types.VoidType;
import me.pesekjak.hippo.classes.types.primitives.*;
import me.pesekjak.hippo.utils.events.MethodCallEvent;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprType extends SimpleExpression<Type> {

    private Expression<Object> typeExpression;
    private int arraySize = 0;

    static {
        Skript.registerExpression(
                ExprType.class, Type.class, ExpressionType.COMBINED,
                "%-primitivetype/javatype%<^(\\[\\])*>" // Primitive has to be first to keep the priority
        );
    }

    @Override
    protected Type @NotNull [] get(@NotNull Event event) {
        Object typeObject = typeExpression.getSingle(event);
        Type type;
        if(typeObject == null) return new Type[0];
        if(typeObject instanceof Primitive primitive) {
            if(primitive == Primitive.VOID && arraySize > 0) {
                Skript.error("Void types can't create array");
                return new Type[0];
            }
            type = typeFromPrimitive(primitive);
        } else {
            type = typeFromObject(typeObject);
        }
        if(type == null) return new Type[0];
        for (int i = 0; i < arraySize; ++i) {
            type = type.array();
        }
        return new Type[] {type};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Type> getReturnType() {
        return Type.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        Type type = get(event)[0];
        return "type of " + type.dotPath();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if(!getParser().isCurrentEvent(NewSkriptClassEvent.class, MethodCallEvent.class))
            return false;
        typeExpression = SkriptUtil.defendExpression(expressions[0]);
        if(parseResult.regexes.size() > 0) {
            arraySize = parseResult.regexes.get(0).group().length() / 2;
        }
        return true;
    }

    public static Type typeFromPrimitive(Primitive primitive) {
        if(primitive == null) return null;
        return switch (primitive) {
            case BOOLEAN -> new BooleanType();
            case CHAR -> new CharType();
            case BYTE -> new ByteType();
            case SHORT -> new ShortType();
            case INT -> new IntType();
            case LONG -> new LongType();
            case FLOAT -> new FloatType();
            case DOUBLE -> new DoubleType();
            case VOID -> new VoidType();
        };
    }

    public static Type typeFromObject(Object o) {
        if(o instanceof Type) return (Type) o;
        if(o instanceof JavaType) return new NonPrimitiveType((JavaType) o);
        return null;
    }

}
