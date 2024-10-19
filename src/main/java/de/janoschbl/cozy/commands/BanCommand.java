package de.janoschbl.cozy.commands;

import de.janoschbl.cozy.Main;
import de.janoschbl.cozy.managers.PunishmentManager;
import de.janoschbl.cozy.utils.MessageUtils;
import de.janoschbl.cozy.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

public class BanCommand extends Command implements TabExecutor {

    public BanCommand() {
        super("ban");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ban.use")) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("no-permission")));
            return;
        }
        if (args.length < 1) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Verwendung: &c/ban <Spieler> &7[Punkte] [Dauer] [Grund]")));
            return;
        }
        String target = args[0];
        if (UUIDFetcher.getUUID(target) == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Dieser Spieler existiert nicht?!")));
            return;
        }
        int points = Main.getInstance().getConfigManager().getConfig().getInt("points.punishments.ban_default");
        String reason = "Kein Grund angegeben";
        String durationStr = "Permanent";

        if (args.length >= 2) {
            try {
                points = Integer.parseInt(args[1]);
                durationStr = args[2];
                if (args.length >= 4) {
                    reason = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                }
            } catch (NumberFormatException e) {
                reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            }
        }

        long duration = PunishmentManager.parseDuration(durationStr);
        PunishmentManager.banPlayer(sender, target, points, reason, duration);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ban.use")) {
            return Collections.emptySet();
        }
        if (args.length == 1) {
            Set<String> suggestions = new HashSet<>();
            String prefix = args[0].toLowerCase();
            for (ProxiedPlayer player : Main.getInstance().getProxy().getPlayers()) {
                String name = player.getName();
                if (name.toLowerCase().startsWith(prefix)) {
                    suggestions.add(name);
                }
            }
            return suggestions;
        } else if (args.length == 2) {
            Set<String> suggestions = new HashSet<>();
            String prefix = args[1];
            for (int i = 1; i <= 100; i++) {
                String num = String.valueOf(i);
                if (num.startsWith(prefix)) {
                    suggestions.add(num);
                }
            }
            return suggestions;
        } else if (args.length == 3) {
            Set<String> durationSuggestions = new HashSet<>();
            String prefix = args[2].toLowerCase();
            String[] durations = {"Permanent", "12h", "24h", "1d", "7d", "14d", "30d", "48h"};
            for (String duration : durations) {
                if (duration.toLowerCase().startsWith(prefix)) {
                    durationSuggestions.add(duration);
                }
            }
            return durationSuggestions;
        }
        return Collections.emptySet();
    }
}
