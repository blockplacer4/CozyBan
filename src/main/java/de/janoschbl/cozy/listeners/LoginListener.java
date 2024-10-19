package de.janoschbl.cozy.listeners;

import de.janoschbl.cozy.Main;
import de.janoschbl.cozy.managers.PunishmentManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginListener implements Listener {

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (!Main.getInstance().isPluginEnabled()) {
            return;
        }

        String uuid = event.getConnection().getUniqueId().toString();

        if (PunishmentManager.isPlayerBanned(uuid)) {
            String banReason = PunishmentManager.getBanReason(uuid);
            event.setCancelled(true);
            event.setReason(new TextComponent(banReason));
        }
    }
}
