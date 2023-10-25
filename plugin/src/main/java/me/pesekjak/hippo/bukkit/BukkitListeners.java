package me.pesekjak.hippo.bukkit;

import ch.njol.skript.events.bukkit.PreScriptLoadEvent;
import me.pesekjak.hippo.utils.TypeLookup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Implements Bukkit listeners to register.
 */
public final class BukkitListeners implements Listener {

    /**
     * Script preload listener that handles the
     * un-registration of pre-imports so new
     * pre-imports can be registered.
     *
     * @param event event
     */
    @EventHandler
    public void onPreScriptLoad(PreScriptLoadEvent event) {
        TypeLookup.unregisterPreImports(event.getScripts());
    }

}
