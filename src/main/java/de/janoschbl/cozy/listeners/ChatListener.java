package de.janoschbl.cozy.listeners;

import de.janoschbl.cozy.Main;
import de.janoschbl.cozy.managers.PunishmentManager;
import de.janoschbl.cozy.utils.MessageUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!Main.getInstance().isPluginEnabled()) {
            return;
        }

        if (!(event.getSender() instanceof ProxiedPlayer player)) {
            return;
        }
        String uuid = player.getUniqueId().toString();

        if (PunishmentManager.isPlayerMuted(uuid)) {
            String muteReason = PunishmentManager.getMuteReason(uuid);
            player.sendMessage(new TextComponent(muteReason));
            event.setCancelled(true);
        }
    }
}
