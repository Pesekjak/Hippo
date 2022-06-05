package me.pesekjak.hippo.skript.utils.syntax.utils;

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
import me.pesekjak.hippo.classes.Primitive;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.Reflectness;
import me.pesekjak.hippo.utils.SkriptUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

@Name("Multidimensional Array")
@Description("Multidimensional Java Array with defined size and elements.")
@Since("1.0-BETA.1")
public class ExprArray extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprArray.class, Object.class, ExpressionType.COMBINED,
                "new array %primitive%<^(\\[\\])*>\\(%number%\\)\\([%-objects%]\\)",
        "new array %-javatype%<^(\\[\\])*>\\(%number%\\)\\([%-objects%]\\)"
        );
    }

    private Expression<?> typeExpression;
    private Expression<Number> sizeExpression;
    private Expression<Object> objectsExpression;
    private int arraySize = 0;
    private int pattern;

    @Override
    protected Object @NotNull [] get(@NotNull Event event) {
        if(sizeExpression.getSingle(event) == null) return new Object[0];
        Class<?> typeClass;
        Primitive primitive = null;
        if(pattern == 0) {
            primitive = (Primitive) typeExpression.getSingle(event);
            if(primitive == null) return new Object[0];
            typeClass = primitive.getPrimitiveClass();
        } else {
            Type type = SkriptClassBuilder.getTypeFromExpression(typeExpression);
            if (type == null) return new Object[0];
            try {
                typeClass = SkriptReflectHook.getLibraryLoader().loadClass(type.getDotPath());
            } catch (ClassNotFoundException e) { return new Object[0]; }
        }
        if(typeClass == null) return new Object[0];
        if(sizeExpression.getSingle(event) == null) return new Object[0];
        for (int i = 0; i < arraySize - 1; ++i) {
            typeClass = typeClass.arrayType();
        }
        Object array = Array.newInstance(typeClass, sizeExpression.getSingle(event).intValue());
        int i = 0;
        if(objectsExpression != null) {
            Primitive counterPrimitive = Primitive.NONE;
            Method convertorMethod = null;
            if(pattern == 1 && Number.class.isAssignableFrom(typeClass)) {
                for(Primitive aPrimitive : Primitive.values()) {
                    if(aPrimitive.getClassCounterpart() == typeClass) {
                        counterPrimitive = aPrimitive;
                        convertorMethod = Reflectness.getMethod(typeClass, "valueOf", counterPrimitive.getPrimitiveClass());
                        break;
                    }
                }
            }
            for(Object object : objectsExpression.getAll(event)) {
                object = SkriptReflectHook.unwrap(object);
                if(pattern == 0 && arraySize == 1) object = new Constant(object).getConstantObject(primitive);
                if(pattern == 1) {
                    if(Number.class.isAssignableFrom(typeClass)) {
                        object = new Constant(object).getConstantObject(counterPrimitive);
                        object = Reflectness.invoke(convertorMethod, null, object);
                    } else {
                        object = typeClass.cast(object);
                    }
                }
                Array.set(array, i, object);
                i++;
            }
        }
        return new Object[] {SkriptReflectHook.wrap(array)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return SkriptReflectHook.getObjectWrapperClass();
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "java array";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pattern = i;
        typeExpression = SkriptUtils.defendExpression(expressions[0]);
        sizeExpression = SkriptUtils.defendExpression(expressions[1]);
        objectsExpression = SkriptUtils.defendExpression(expressions[2]);
        if(parseResult.regexes.size() > 0) {
            arraySize = parseResult.regexes.get(0).group().length() / 2;
        }
        return true;
    }
}
