package me.pesekjak.hippo.skript.utils.syntax.options;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import me.pesekjak.hippo.classes.builder.ClassBuilder;
import me.pesekjak.hippo.utils.SkriptUtils;
import me.pesekjak.hippo.utils.events.HippoOptionsEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

@Name("Hippo's Java Version")
@Description("Changes Java Version option for Hippo's class builder.")
@Since("1.0-BETA.1")
public class EffJavaVersion extends Effect {

    static {
        Skript.registerEffect(EffJavaVersion.class,
                "java version: %-number%"
        );
    }

    Expression<Number> numberExpression;

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "java version";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if (!getParser().isCurrentEvent(HippoOptionsEvent.class)) return false;
        numberExpression = SkriptUtils.defendExpression(expressions[0]);
        if (numberExpression.getSingle(null) == null) return false;
        int version = numberExpression.getSingle(null).intValue();
        if (!(version > 0 && version < 19)) {
            Skript.error("Java version has to be between 1 and 18!");
            return false;
        }
        int javaVersion = Opcodes.V1_2 - 2 + version;
        if(version == 1) javaVersion = Opcodes.V1_1;
        ClassBuilder.JAVA_VERSION = javaVersion;
        return true;
    }
}
