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

@Name("Hippo's ASM Version")
@Description("Changes ASM Version option for Hippo's class builder.")
@Since("1.0-BETA.1")
public class EffASMVersion extends Effect {

    static {
        Skript.registerEffect(EffASMVersion.class,
                "asm version: %-number%"
        );
    }

    Expression<Number> numberExpression;

    @Override
    protected void execute(@NotNull Event event) {

    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "asm version";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        if (!getParser().isCurrentEvent(HippoOptionsEvent.class)) return false;
        numberExpression = SkriptUtils.defendExpression(expressions[0]);
        if(numberExpression.getSingle(null) == null) return false;
        int version = numberExpression.getSingle(null).intValue();
        if(!(version > 3 && version < 11)) {
            Skript.error("ASM version has to be between 4 and 10!");
            return false;
        }
        int asmVersion = Opcodes.ASM9;
        switch (version) {
            case 4 -> asmVersion = Opcodes.ASM4;
            case 5 -> asmVersion = Opcodes.ASM5;
            case 6 -> asmVersion = Opcodes.ASM6;
            case 7 -> asmVersion = Opcodes.ASM7;
            case 8 -> asmVersion = Opcodes.ASM8;
            case 9 -> asmVersion = Opcodes.ASM9;
            case 10 -> asmVersion = Opcodes.ASM10_EXPERIMENTAL;
        }
        ClassBuilder.ASM_VERSION = asmVersion;
        return true;
    }
}
