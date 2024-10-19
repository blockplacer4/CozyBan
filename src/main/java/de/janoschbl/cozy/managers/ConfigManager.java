package de.janoschbl.cozy.managers;

import de.janoschbl.cozy.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigManager {
    private final File configFile;
    private Configuration config;
    private Configuration messages;

    public ConfigManager() {
        this.configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
        loadConfig();
        loadMessages();
    }

    private void loadConfig() {
        if (!Main.getInstance().getDataFolder().exists()) {
            Main.getInstance().getDataFolder().mkdir();
        }
        File file = new File(Main.getInstance().getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = Main.getInstance().getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                Main.getInstance().getLogger().warning(e.toString());
            }
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
    }

    private void loadMessages() {
        File file = new File(Main.getInstance().getDataFolder(), "messages.yml");
        if (!file.exists()) {
            try (InputStream in = Main.getInstance().getResourceAsStream("messages.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                Main.getInstance().getLogger().warning(e.toString());
            }
        }
        try {
            messages = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
    }

    public void reloadConfig() throws IOException {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            Main.getInstance().getLogger().severe("Failed config.yml reload");
            Main.getInstance().getLogger().warning(e.toString());
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public Configuration getMessages() {
        return messages;
    }
}
