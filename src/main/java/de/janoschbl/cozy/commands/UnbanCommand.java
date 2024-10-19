package de.janoschbl.cozy.commands;

import de.janoschbl.cozy.managers.PunishmentManager;
import de.janoschbl.cozy.utils.MessageUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;

public class UnbanCommand extends Command implements TabExecutor {

    public UnbanCommand() {
        super("unban", "unban.use");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("unban.use")) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("no-permission")));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Verwendung: &c/unban <Spieler>")));
            return;
        }

        String targetName = args[0];
        PunishmentManager.unbanPlayer(sender, targetName);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("unban.use")) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            return PunishmentManager.getBannedPlayers();
        }
        return new ArrayList<>();
    }
}
