package me.pesekjak.hippo.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Arrays;

public class Logger {

    private final static java.util.logging.Logger LOGGER = Bukkit.getLogger();
    private final static String PREFIX = "&3[&7Hippo&3] ";

    private Logger() {
        throw new UnsupportedOperationException();
    }

    private static String getFormattedString(String message) {
        return ChatColor.translateAlternateColorCodes('&', PREFIX + message);
    }

    public static void info(String... messages) {
        LOGGER.info(getFormattedString(ChatColor.WHITE + messages[0]));
        if (messages.length > 1)
            Arrays.stream(messages)
                    .skip(1)
                    .forEachOrdered(Logger::info);
    }

    public static void warn(String... messages) {
        LOGGER.warning(getFormattedString(ChatColor.GOLD + messages[0]));
        if (messages.length > 1)
            Arrays.stream(messages)
                    .skip(1)
                    .forEachOrdered(Logger::warn);
    }

    public static void severe(String... messages) {
        LOGGER.severe(getFormattedString(ChatColor.RED + messages[0]));
        if (messages.length > 1)
            Arrays.stream(messages)
                    .skip(1)
                    .forEachOrdered(Logger::severe);
    }

}
