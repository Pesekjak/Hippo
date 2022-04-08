package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.ClassType;
import me.pesekjak.hippo.classes.SkriptClass;
import me.pesekjak.hippo.classes.classtypes.*;
import me.pesekjak.hippo.classes.registry.SkriptClassRegistry;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EffSkriptClassType extends Effect {

    static {
        Skript.registerEffect(EffSkriptClassType.class,
                "type: %skriptclasstype%"
        );
    }

    private Expression<ClassType> classTypeExpression;

    @Override
    protected void execute(@NotNull Event event) {
        if (SkriptClassRegistry.REGISTRY.getSkriptClass(((NewSkriptClassEvent) event).getClassName()) != null) return;
        ClassType classType = classTypeExpression.getSingle(event);
        if (classType == null) return;
        SkriptClass newClass = null;
        switch (classType) {
            case CLASS -> newClass = new TypeClass(((NewSkriptClassEvent) event).toType());
            case INTERFACE -> newClass = new TypeInterface(((NewSkriptClassEvent) event).toType());
            case RECORD -> newClass = new TypeRecord(((NewSkriptClassEvent) event).toType());
            case ENUM -> newClass = new TypeEnum(((NewSkriptClassEvent) event).toType());
            case ANNOTATION -> newClass = new TypeAnnotation(((NewSkriptClassEvent) event).toType());
        }
        SkriptClassRegistry.REGISTRY.addSkriptClass(((NewSkriptClassEvent) event).getClassName(), newClass);
        SkriptClassBuilder.setRegisteringClass(newClass);
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "skript class type";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        classTypeExpression = SkriptUtils.defendExpression(expressions[0]);
        return getParser().isCurrentEvent(NewSkriptClassEvent.class) && SkriptUtils.canInit(classTypeExpression);
    }
}
