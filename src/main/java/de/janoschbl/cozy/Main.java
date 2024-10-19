package de.janoschbl.cozy;

import de.janoschbl.cozy.commands.*;
import de.janoschbl.cozy.listeners.ChatListener;
import de.janoschbl.cozy.listeners.LoginListener;
import de.janoschbl.cozy.managers.ConfigManager;
import de.janoschbl.cozy.managers.DatabaseManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

public class Main extends Plugin {

    private static Main instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private boolean pluginEnabled = true;
    private boolean floodgateInstalled;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager();
        databaseManager = new DatabaseManager();
        databaseManager.connect();

        floodgateInstalled = getProxy().getPluginManager().getPlugin("floodgate") != null;

        ProxyServer.getInstance().getPluginManager().unregisterCommands(this);

        if (floodgateInstalled) {
            getLogger().info("Floodgate wurde erkannt und aktiviert");
        } else {
            getLogger().info("Floodgate wurde nicht gefunden. Bedrock-Spieler werden möglicherweise nicht korrekt behandelt.");
        }

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        databaseManager.disconnect();
    }

    public void registerCommands() {
        getProxy().getPluginManager().registerCommand(this, new CozyBanCommand());
        getProxy().getPluginManager().registerCommand(this, new BanCommand());
        getProxy().getPluginManager().registerCommand(this, new MuteCommand());
        getProxy().getPluginManager().registerCommand(this, new WarnCommand());
        getProxy().getPluginManager().registerCommand(this, new KickCommand());
        getProxy().getPluginManager().registerCommand(this, new TBanCommand());
        getProxy().getPluginManager().registerCommand(this, new HistoryCommand());
        getProxy().getPluginManager().registerCommand(this, new UnbanCommand());
        getProxy().getPluginManager().registerCommand(this, new UnmuteCommand());
    }

    public void registerListeners() {
        getProxy().getPluginManager().registerListener(this, new ChatListener());
        getProxy().getPluginManager().registerListener(this, new LoginListener());
    }

    public void reloadConfig() throws IOException {
        configManager.reloadConfig();
    }

    public static Main getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public boolean isFloodgateInstalled() {
        return floodgateInstalled;
    }

    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    public void setPluginEnabled(boolean enabled) {
        this.pluginEnabled = enabled;
    }

    public void getLogger(String string) {
    }
}
