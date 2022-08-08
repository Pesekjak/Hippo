package me.pesekjak.hippo.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.Variables;
import me.pesekjak.hippo.Hippo;
import me.pesekjak.hippo.skript.classes.annotations.PreScriptLoadListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public class SkriptUtil {

    private static final boolean skript2_6 = Skript.classExists("ch.njol.skript.lang.parser.ParserInstance") || !Skript.methodExists(ParserInstance.class, "get");

    private SkriptUtil() {
        throw new UnsupportedOperationException();
    }

    public static boolean isSkript2_6() {
        return skript2_6;
    }

    public static void setVariable(Variable<?> variable, Event e, Object o) {
        Variables.setVariable(variable.getName().toString(e), o, e, variable.isLocal());
    }

    public static void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PreScriptLoadListener(), Hippo.getInstance());
    }

}
