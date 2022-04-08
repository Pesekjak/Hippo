package me.pesekjak.hippo.skript.classes;

import me.pesekjak.hippo.classes.Argument;
import me.pesekjak.hippo.classes.PrimitiveType;
import me.pesekjak.hippo.classes.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Pair of String and Hippo's Type/PrimitiveType, primitive parameter can't be null,
 * in case Type is used, PrimitiveType has to be NONE, in case PrimitiveType is used,
 * Type can be null. Used for fields (Type + name), methods (Return Type + name) and
 * parameters (Type + parameter name)
 */
public record Pair(@NotNull PrimitiveType primitive, @Nullable Type type, String name) {

    public PrimitiveType getPrimitiveType() { return primitive; }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Argument asArgument() {
        return new Argument(primitive, type, name);
    }

}
