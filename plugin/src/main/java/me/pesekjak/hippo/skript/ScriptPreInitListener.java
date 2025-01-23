package me.pesekjak.hippo.skript;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Config;
import me.pesekjak.hippo.utils.TypeLookup;

import java.util.Collection;

/**
 * Script preload listener that handles the
 * un-registration of pre-imports so new
 * pre-imports can be registered.
 */
public class ScriptPreInitListener implements ScriptLoader.ScriptPreInitEvent {

    @Override
    public void onPreInit(Collection<Config> configs) {
        TypeLookup.unregisterPreImports(configs);
    }

}
