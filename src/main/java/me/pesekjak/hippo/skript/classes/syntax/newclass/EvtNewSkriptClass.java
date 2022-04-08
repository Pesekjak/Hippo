package me.pesekjak.hippo.skript.classes.syntax.newclass;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import me.pesekjak.hippo.classes.Type;
import me.pesekjak.hippo.classes.registry.SkriptClassRegistry;
import me.pesekjak.hippo.skript.classes.SkriptClassBuilder;
import me.pesekjak.hippo.utils.events.NewSkriptClassEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EvtNewSkriptClass extends SelfRegisteringSkriptEvent {

    static {
        Skript.registerEvent("Create new Skript Class", EvtNewSkriptClass.class, NewSkriptClassEvent.class,
                "(create|register|define) [new] [skript][(-| )]class <([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*>"
        );
    }

    private Type type;

    private boolean isRegistered;

    @Override
    public void register(@NotNull Trigger trigger) {
        NewSkriptClassEvent event = new NewSkriptClassEvent(type.getDotPath());
        SkriptClassBuilder.setRegisteringEvent(event);
        trigger.execute(event);
    }

    @Override
    public void unregister(@NotNull Trigger trigger) {
        if(isRegistered) SkriptClassRegistry.REGISTRY.skriptClassMap.remove(type.getDotPath());
    }

    @Override
    public void unregisterAll() {
        if(isRegistered) SkriptClassRegistry.REGISTRY.skriptClassMap.remove(type.getDotPath());
    }

    @Override
    public boolean init(Literal<?> @NotNull [] literals, int i, SkriptParser.@NotNull ParseResult parseResult) {
        String className = parseResult.regexes.get(0).group();
        if (SkriptClassRegistry.REGISTRY.skriptClassMap.containsKey(className)) {
            Skript.error("Class with name '" + className + "' is already registered");
            return false;
        }

        type = new Type(className);

        SkriptClassRegistry.REGISTRY.addSkriptClass(className, null);
        isRegistered = true;

        return true;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "new skript class";
    }
}
