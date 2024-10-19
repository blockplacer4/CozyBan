package de.janoschbl.cozy.listeners;

import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabCompleteListener implements Listener {

    @EventHandler
    public void onTabCompleteResponse(TabCompleteResponseEvent event) {
        event.setCancelled(true);
        }
}


