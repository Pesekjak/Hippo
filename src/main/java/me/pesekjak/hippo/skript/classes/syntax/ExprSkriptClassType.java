package me.pesekjak.hippo.skript.classes.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ExprSkriptClassType extends SimpleExpression<ClassType> {

    static {
        Skript.registerExpression(ExprSkriptClassType.class, ClassType.class, ExpressionType.SIMPLE,
                "class",
                "interface",
                "record",
                "enum",
                "annotation"
        );
    }

    private int pattern;

    @Override
    protected ClassType @NotNull [] get(@NotNull Event event) {
        ClassType classType = null;
        switch (pattern) {
            case 0 -> classType = ClassType.CLASS;
            case 1 -> classType = ClassType.INTERFACE;
            case 2 -> classType = ClassType.RECORD;
            case 3 -> classType = ClassType.ENUM;
            case 4 -> classType = ClassType.ANNOTATION;
        }
        return new ClassType[] { classType };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends ClassType> getReturnType() {
        return ClassType.class;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "class type";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        pattern = i;
        return getParser().isCurrentEvent(NewSkriptClassEvent.class);
    }
}
