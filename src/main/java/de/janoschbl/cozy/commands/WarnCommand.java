package de.janoschbl.cozy.commands;

import de.janoschbl.cozy.Main;
import de.janoschbl.cozy.managers.PunishmentManager;
import de.janoschbl.cozy.utils.MessageUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class WarnCommand extends Command implements TabExecutor {

    public WarnCommand() {
        super("warn", "warn.use");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("warn.use")) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("no-permission")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cVerwendung: /warn <Spieler> <Grund>")));
            return;
        }
        String target = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        PunishmentManager.warnPlayer(sender, target, reason);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("warn.use")) {
            return new HashSet<>();
        }
        Set<String> suggestions = new HashSet<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (ProxiedPlayer player : Main.getInstance().getProxy().getPlayers()) {
                String name = player.getName();
                if (name.toLowerCase().startsWith(prefix)) {
                    suggestions.add(name);
                }
            }
        }
        return suggestions;
    }
}
