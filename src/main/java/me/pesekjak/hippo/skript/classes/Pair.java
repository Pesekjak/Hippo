package me.pesekjak.hippo.skript.classes;

import me.pesekjak.hippo.classes.Argument;
import me.pesekjak.hippo.classes.PrimitiveType;
import me.pesekjak.hippo.classes.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
