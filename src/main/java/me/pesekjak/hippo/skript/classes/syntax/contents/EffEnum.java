package me.pesekjak.hippo.skript.classes.syntax.contents;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.*;
import me.pesekjak.hippo.classes.classtypes.TypeEnum;
import me.pesekjak.hippo.classes.contents.Enum;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("Enum")
@Description("Creates new Enum for the current enum class.")
@Since("1.0-BETA.1")
public class EffEnum extends Effect {

    static {
        Skript.registerEffect(EffEnum.class,
                "enum <[a-zA-Z0-9]*>\\(%-objects%\\) this\\(%-objects%\\)",
                         "enum <[a-zA-Z0-9]*>\\(\\) this\\(\\)"
        );
    }

    private String enumName;
    private Expression<Object> argumentExpression;
    private Expression<Object> superExpression;

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "enum";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        enumName = parseResult.regexes.get(0).group();
        if(i == 0) {
            argumentExpression = SkriptUtils.defendExpression(expressions[0]);
            superExpression = SkriptUtils.defendExpression(expressions[1]);
        }
        if (!getParser().isCurrentEvent(NewSkriptClassEvent.class)) return false;
        return build(SkriptClassBuilder.getCurrentEvent());
    }

    protected boolean build(@NotNull Event event) {
        SkriptClass skriptClass = ((NewSkriptClassEvent) event).getSkriptClass();
        if(!(skriptClass instanceof TypeEnum)) {
            Skript.error("You can't add enums to '" + ((NewSkriptClassEvent) event).getSkriptClass().getClassName() + "' because type of the class isn't enum");
            return false;
        }
        Enum enumField = new Enum(skriptClass.getType(), enumName);
        if(argumentExpression != null) {
            enumField.setValue(argumentExpression);
        }
        if(superExpression != null) {
            int i = 0;
            for(Object superArgumentObject : superExpression.getAll(event)) {
                i++;
                if(superArgumentObject instanceof Type type) {
                    enumField.addSuperArgument(new Argument(type, "A" + i));
                } else if(superArgumentObject instanceof PrimitiveType type) {
                    enumField.addSuperArgument(new Argument(type, "A" + i));
                } else if(superArgumentObject instanceof Primitive) {
                    enumField.addSuperArgument(new Argument(new PrimitiveType((Primitive) superArgumentObject), "A" + i));
                } else {
                    Class<?> classInstance = SkriptReflectHook.classOfJavaType(superArgumentObject);
                    if(classInstance == null) return false;
                    enumField.addSuperArgument(new Argument(new Type(classInstance), "A" + i));
                }
            }
        }
        if(SkriptClassBuilder.getRegisteringClass().getField(enumName) != null) {
            Skript.error("Enum '" + enumName + "' already exists for class '" + SkriptClassBuilder.getRegisteringClass().getClassName() + "'");
            return false;
        }
        ((NewSkriptClassEvent) event).getStackedAnnotations().forEach(enumField::addAnnotation);
        ((NewSkriptClassEvent) event).clearStackedAnnotations();
        SkriptClassBuilder.getRegisteringClass().addField(enumName, enumField);
        return true;
    }
}
