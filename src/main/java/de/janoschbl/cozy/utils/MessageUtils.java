package de.janoschbl.cozy.utils;

import de.janoschbl.cozy.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;

public class MessageUtils {

    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatWithPrefix(String message) {
        String prefix = Main.getInstance().getConfigManager().getConfig().getString("prefix");
        return format(prefix + message);
    }

    public static String getMessage(String path) {
        Configuration messages = Main.getInstance().getConfigManager().getMessages();
        String message = messages.getString(path);
        return format(message);
    }

    public static String getMessageWithPrefix(String path) {
        String message = getMessage(path);
        return formatWithPrefix(message);
    }

    public static String replacePlaceholders(String message, String[] placeholders, String[] values) {
        for (int i = 0; i < placeholders.length; i++) {
            message = message.replace("%" + placeholders[i] + "%", values[i]);
        }
        return format(message);
    }
}
