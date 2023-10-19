package me.pesekjak.hippo;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import me.pesekjak.hippo.bukkit.BukkitListeners;
import me.pesekjak.hippo.core.loader.DynamicClassLoader;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;

/**
 * Main class handling the logic of a Bukkit plugin.
 */
public class Hippo extends JavaPlugin {

    //  .-''''-. _
    // ('    '  '0)-/)
    // '..____..:    :._
    //   .u  u (        '-..------._
    //   |     /      :   '.        '--.
    //  .nn_nn/ (      :   '            ':
    // ( '' '' /      ;     .            ':
    //  ''----' ":          :            : '.
    //         .'/                           '.
    //        / /                             '.
    //       /_|       )                     .\|
    //         |      /:.                    . '
    //         '--.__|  '--._  ,            /
    //                      /'-,          .'
    //                     /   |        _.'
    //                    (____.       /
    //                          :      :
    //                           '-'-'-'
    // MIGHTY SUPER HIPPO FROM THE HIPO LAKE
    // WHERE HIPPOS, ELEPHANTS, WATER BUFFALO
    // AND OTHER ANIMALS LIVE.

    private static Hippo instance;
    private static SkriptAddon addonInstance;

    private int javaClassFileVersion = Opcodes.V17;

    /**
     * @return current instance of the plugin
     */
    public static Hippo getInstance() {
        return instance;
    }

    /**
     * @return current instance of the SkriptAddon.
     */
    public static SkriptAddon getAddonInstance() {
        return addonInstance;
    }

    @Override
    public void onEnable() {
        instance = this;
        if (!Bukkit.getPluginManager().isPluginEnabled("Skript")) {
            getLogger().severe("Skript could not be found. Disabling Hippo...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            addonInstance = Skript.registerAddon(getInstance()).setLanguageFileDirectory("lang");
            addonInstance.loadClasses("me.pesekjak.hippo.elements");
            Class.forName(DynamicClassLoader.class.getName(), true, Hippo.class.getClassLoader());
        } catch (IOException | ClassNotFoundException exception) {
            getLogger().severe("Failed to load Hippo classes. Disabling Hippo...");
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException(exception);
        }

        Bukkit.getPluginManager().registerEvents(new BukkitListeners(), this);
        loadConfig();
    }

    public int getJavaClassFileVersion() {
        return javaClassFileVersion;
    }

    private void loadConfig() {
        saveDefaultConfig();
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(new File(getDataFolder(), "config.yml"));
        } catch (Throwable exception) {
            getLogger().severe("Failed to load the configuration");
        }

        if (config.contains("java_classfile_version") && config.isInt("java_classfile_version")) {
            int value = config.getInt("java_classfile_version");

            if (value > 0 && value < 22) {
                javaClassFileVersion = value == 1 ? Opcodes.V1_1 : 46 + value - 2;
            } else {
                getLogger().warning("ClassFile version " + value + " specified in the config is not valid");
            }
        } else {
            getLogger().warning("Config file is missing 'java_classfile_version' entry");
        }
    }

}