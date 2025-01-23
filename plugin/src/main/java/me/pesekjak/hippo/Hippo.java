package me.pesekjak.hippo;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.parser.ParserInstance;
import com.btk5h.skriptmirror.util.ReflectCacheInjector;
import me.pesekjak.hippo.core.loader.DynamicClassLoader;
import me.pesekjak.hippo.elements.classes.Types;
import me.pesekjak.hippo.skript.ClassUpdateParserData;
import me.pesekjak.hippo.skript.ScriptPreInitListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.Opcodes;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.util.ClassLoader;

import java.io.File;
import java.util.logging.Level;

/**
 * Main class handling the logic of a Bukkit plugin.
 */
@SuppressWarnings("UnstableApiUsage")
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

    public static final String ADDON_NAME = "Hippo";

    private static Hippo instance;
    private static SkriptAddon addonInstance;

    private int javaClassFileVersion = Opcodes.V17;
    private boolean disablesCache = false;

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
        addonInstance = Skript.instance().registerAddon(getClass(), ADDON_NAME);
        addonInstance.localizer().setSourceDirectories("lang", null);

        loadConfig();

        if (!Types.register()) {
            getLogger().severe("Failed to register required types, the addon can not function properly");
            getLogger().severe("This is most likely caused by another addon incompatible with Hippo");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            ClassLoader.loadClasses(Hippo.class, getFile(), "me.pesekjak.hippo", "elements");
            ParserInstance.registerData(ClassUpdateParserData.class, ClassUpdateParserData::new);
            ScriptLoader.eventRegistry().register(new ScriptPreInitListener());
            DynamicClassLoader.injectReflectClassLoader();
            if (disablesCache) ReflectCacheInjector.inject();
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "Failed to load Hippo classes. Disabling Hippo...", exception);
            Bukkit.getPluginManager().disablePlugin(this);
        }
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

            if (value >= 1 && value <= 23) {
                javaClassFileVersion = value == 1 ? Opcodes.V1_1 : 46 + value - 2;
            } else {
                getLogger().warning("ClassFile version " + value + " specified in the config is not valid");
            }
        } else {
            getLogger().warning("Config file is missing 'java_classfile_version' entry");
        }

        if (config.contains("disable_cache") && config.isBoolean("disable_cache")) {
            disablesCache = config.getBoolean("disable_cache");
        } else {
            getLogger().warning("Config file is missing 'disable_cache' entry");
        }
    }

}
