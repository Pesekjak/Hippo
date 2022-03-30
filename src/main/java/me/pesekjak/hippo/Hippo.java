package me.pesekjak.hippo;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import me.pesekjak.hippo.hooks.SkriptReflectHook;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.SkriptUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class Hippo extends JavaPlugin {

    private static Hippo instance;
    private static SkriptAddon addonInstance;

    public Hippo() {
        instance = this;
    }

    public static Hippo getInstance() {
        if (instance == null) throw new IllegalStateException();
        return instance;
    }

    public static SkriptAddon getAddonInstance() {
        if (addonInstance == null) addonInstance = Skript.registerAddon(getInstance());
        return addonInstance;
    }

    @Override
    public void onEnable() {
        Logger.info("Hippo is slowly waking up!");

        if (!SkriptUtils.isSkriptRunning()) {
            Logger.severe("Hippo went to sleep again because Skript is disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!SkriptUtils.isSkript2_6()) {
            Logger.severe("Your version of Skript (" + Skript.getVersion() + ") is unsupported, at least Skript 2.6 is required to wake up Hippo.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if(!SkriptReflectHook.setup()) {
            Logger.severe("Skript-reflect addon is required for Hippo to wake up.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        addonInstance = Skript.registerAddon(getInstance());
        setupSyntax();
        Logger.info("Hippo woke up from his sleep by the lake and is going to help you.");
    }

    @Override
    public void onDisable() {
        Logger.info("The hippo went to sleep by the lake.");
    }

    public void setupSyntax() {
        try {
            getAddonInstance().loadClasses("me.pesekjak.hippo.skript");
        } catch (IOException exception) { exception.printStackTrace(); }
    }

}
