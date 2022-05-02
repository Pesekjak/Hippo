package me.pesekjak.hippo.skript.preimport.syntax;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import me.pesekjak.hippo.preimport.PreImportManager;
import me.pesekjak.hippo.utils.events.PreImportEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class EvtPreImport extends SelfRegisteringSkriptEvent {

    static {
        Skript.registerEvent("preimport", EvtPreImport.class, PreImportEvent.class,
                "pre[-]import"
        );
    }

    private Config script;

    @Override
    public void register(@NotNull Trigger trigger) {

    }

    @Override
    public void unregister(@NotNull Trigger trigger) {
        PreImportManager.MANAGER.getPreImporting(script).clear();
        PreImportManager.MANAGER.removePreImportingScript(script);
    }

    @Override
    public void unregisterAll() {
        PreImportManager.MANAGER.getPreImporting(script).clear();
        PreImportManager.MANAGER.removePreImportingScript(script);
    }

    @Override
    public boolean init(Literal<?> @NotNull [] literals, int i, SkriptParser.@NotNull ParseResult parseResult) {
        this.script = getParser().getCurrentScript();
        if(PreImportManager.MANAGER.isPreImporting(script)) {
            Skript.error("Each script can have only one pre-import section");
            return false;
        }
        PreImportManager.MANAGER.addPreImportingScript(script);
        return true;
    }

    @Override
    public @NotNull String toString(Event event, boolean b) {
        return "preimporting";
    }
}
