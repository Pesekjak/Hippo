package me.pesekjak.hippo.utils.events.classcontents.constructors;

import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.utils.events.classcontents.ConstructorEvent;

import java.util.HashMap;

public class InitEvent extends ConstructorEvent {

    private final HashMap<Number, Object> superResults;

    public InitEvent() {
        super(null);
        this.superResults = new HashMap<>();
    }

    public HashMap<Number, Object> getSuperResults() {
        return superResults;
    }

    public Object getSuperResult(Number argumentIndex) {
        return SkriptReflectHook.unwrap(superResults.get(argumentIndex));
    }

    public void addSuperResult(Number argumentIndex, Object argument) {
        superResults.putIfAbsent(argumentIndex, argument);
        superResults.replace(argumentIndex, argument);
    }

}
