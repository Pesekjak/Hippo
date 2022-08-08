package me.pesekjak.hippo;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import lombok.Getter;
import me.pesekjak.hippo.utils.Logger;
import me.pesekjak.hippo.utils.SkriptUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

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

    @Getter
    private static Hippo instance;
    @Getter
    private static SkriptAddon addonInstance;

    @Override
    public void onEnable() {
        if(!(Bukkit.getPluginManager().isPluginEnabled("Skript") &&
                Bukkit.getPluginManager().isPluginEnabled("skript-reflect"))) {
            Logger.severe("Hippo requires Skript and skript-reflect installed to wake up!");
            return;
        }
        if(!SkriptUtil.isSkript2_6()) {
            Logger.severe("Hippo requires 2.6 or higher version of Skript to wake up!");
            return;
        }
        if(setupAddon())
            Logger.info("Hippo woke up from his sleep by the hipo lake and is going to help you.");
        else {
            Logger.severe("Hippo failed to wake up and will remain in the hipo lake for some time.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    protected boolean setupAddon() {
        instance = this;
        addonInstance = Skript.registerAddon(getInstance());
        SkriptUtil.registerListeners();
        try {
            getAddonInstance().loadClasses("me.pesekjak.hippo.skript");
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

}
