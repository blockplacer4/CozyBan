package de.janoschbl.cozy.commands;

import de.janoschbl.cozy.Main;
import de.janoschbl.cozy.managers.PunishmentManager;
import de.janoschbl.cozy.utils.MessageUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MuteCommand extends Command implements TabExecutor {

    public MuteCommand() {
        super("mute", "mute.use");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mute.use")) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("no-permission")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Verwendung: &c/mute <Spieler> <Dauer> &7[Grund]")));
            return;
        }
        String target = args[0];
        String durationStr = args[1];
        String reason = "Kein Grund angegeben";
        if (args.length >= 3) {
            reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }

        long duration = PunishmentManager.parseDuration(durationStr);
        PunishmentManager.mutePlayer(sender, target, reason, duration);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mute.use")) {
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
        }

        else if (args.length == 2) {
            Set<String> durationSuggestions = new HashSet<>();
            String prefix = args[1].toLowerCase();
            String[] durations = {"Permanent", "1d", "7d", "14d", "30d", "12h", "24h", "48h"};
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
