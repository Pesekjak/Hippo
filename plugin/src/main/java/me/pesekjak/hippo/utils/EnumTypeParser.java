package me.pesekjak.hippo.utils;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Skript classes parser for Java enum class.
 * @param <T> enum type
 */
public class EnumTypeParser<T extends Enum<T>> extends Parser<T> {

    private final String name;
    private final Class<?> clazz;

    /**
     * @param name name of the type
     * @param clazz enum class
     */
    public EnumTypeParser(String name, Class<?> clazz) {
        this.name = Objects.requireNonNull(name);
        this.clazz = Objects.requireNonNull(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T parse(@NotNull String string, @NotNull ParseContext context) {
        try {
            return Enum.valueOf((Class<T>) clazz, string.toUpperCase());
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public boolean canParse(@NotNull ParseContext context) {
        return true;
    }

    @Override
    public @NotNull String toString(T primitive, int flags) {
        return primitive.name().toLowerCase();
    }

    @Override
    public @NotNull String toVariableNameString(T primitive) {
        return name + ":" + primitive.name().toLowerCase();
    }

}
