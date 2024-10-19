package de.janoschbl.cozy.commands;

import de.janoschbl.cozy.Main;
import de.janoschbl.cozy.managers.PunishmentManager;
import de.janoschbl.cozy.utils.MessageUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;

import java.util.*;

public class TBanCommand extends Command implements TabExecutor {

    private final Map<Integer, String> reasons = new HashMap<>();
    private final Map<Integer, String> durations = new HashMap<>();
    private final Map<Integer, Integer> points = new HashMap<>();
    private final Map<Integer, Boolean> banImmediately = new HashMap<>();

    public TBanCommand() {
        super("tban", "tban.use");
        loadReasons();
    }

    private void loadReasons() {
        Configuration config = Main.getInstance().getConfigManager().getConfig();
        if (!config.getBoolean("tban.enable")) {
            return;
        }
        Configuration reasonsSection = config.getSection("tban.reasons");
        for (String key : reasonsSection.getKeys()) {
            int id = Integer.parseInt(key);
            String reason = reasonsSection.getString(key + ".reason");
            String duration = reasonsSection.getString(key + ".duration");
            int point = reasonsSection.getInt(key + ".points");
            boolean immediate = reasonsSection.getBoolean(key + ".ban_immediately");
            reasons.put(id, reason);
            durations.put(id, duration);
            points.put(id, point);
            banImmediately.put(id, immediate);
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Configuration config = Main.getInstance().getConfigManager().getConfig();
        if (!config.getBoolean("tban.enable")) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Der tban-Befehl ist &cdeaktiviert.")));
            return;
        }

        if (!sender.hasPermission("tban.use")) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("no-permission")));
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Verwendung: &c/tban <Spieler> <Grundnummer>")));
            sendReasonsList(sender);
            return;
        }
        String target = args[0];
        int reasonNumber;
        try {
            reasonNumber = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cUngültige Grundnummer.")));
            return;
        }
        if (!reasons.containsKey(reasonNumber)) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cGrundnummer nicht gültig.")));
            return;
        }
        String reason = reasons.get(reasonNumber);
        long duration = PunishmentManager.parseDuration(durations.get(reasonNumber));
        int point = points.get(reasonNumber);

        if (banImmediately.get(reasonNumber)) {
            duration = -1; // Permanenter Bann
        }

        PunishmentManager.banPlayer(sender, target, point, reason, duration);
    }

    private void sendReasonsList(CommandSender sender) {
        sender.sendMessage(new TextComponent(MessageUtils.format("&8&l----- &c&lBann Gründe &8&l-----")));
        for (int i : reasons.keySet()) {
            String line = MessageUtils.format("&8[&c" + i + "&8] &7" + reasons.get(i));
            sender.sendMessage(new TextComponent(line));
        }
        sender.sendMessage(new TextComponent(MessageUtils.format("&8&l-----------------------------")));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tban.use")) {
            return Collections.emptySet();
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
        } else if (args.length == 2) {
            String prefix = args[1];
            for (int i : reasons.keySet()) {
                String numStr = String.valueOf(i);
                if (numStr.startsWith(prefix)) {
                    suggestions.add(numStr);
                }
            }
        }
        return suggestions;
    }
}
