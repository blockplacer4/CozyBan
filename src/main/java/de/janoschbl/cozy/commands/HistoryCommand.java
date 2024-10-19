package de.janoschbl.cozy.commands;

import de.janoschbl.cozy.managers.PunishmentManager;
import de.janoschbl.cozy.utils.MessageUtils;
import de.janoschbl.cozy.Main;
import de.janoschbl.cozy.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryCommand extends Command implements TabExecutor {

    public HistoryCommand() {
        super("history", "history.use");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("history.use")) {
            sender.sendMessage(new TextComponent(MessageUtils.getMessageWithPrefix("no-permission")));
            return;
        }

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Verwendung: &c/history <Spieler> &7[Seite]")));
            return;
        }

        String targetName = args[0];
        int page = 1;
        if (args.length == 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Ungültige Seitennummer.")));
                return;
            }
        }

        String targetUUID = String.valueOf(UUIDFetcher.getUUID(targetName));
        if (targetUUID == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Spieler nicht gefunden.")));
            return;
        }

        List<String> historyEntries = getHistoryEntries(targetUUID);
        int totalPages = (int) Math.ceil((double) historyEntries.size() / 5);

        if (totalPages == 0) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Dieser Spieler hat keine History")));
            return;
        }

        if (page > totalPages || page < 1) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&7Ungültige Seitennummer. Es gibt insgesamt &c" + totalPages + " Seiten.")));
            return;
        }
        String header = MessageUtils.format("&8&l----- &c&lHistory von " + targetName + " &8&l(" + page + "/" + totalPages + ") &8&l-----");
        sender.sendMessage(new TextComponent(header));
        sender.sendMessage(new TextComponent());
        int startIndex = (page - 1) * 5;
        int endIndex = Math.min(startIndex + 5, historyEntries.size());
        for (int i = startIndex; i < endIndex; i++) {
            sender.sendMessage(new TextComponent(historyEntries.get(i)));
            sender.sendMessage(new TextComponent());
        }
        sender.sendMessage(new TextComponent(MessageUtils.format("&8&l" + "-".repeat(header.replaceAll("&[0-9a-fk-or]", "").length() - 16))));
    }

    private List<String> getHistoryEntries(String uuid) {
        List<String> entries = new ArrayList<>();
        String query = "SELECT * FROM punishments WHERE uuid = ? ORDER BY created_at DESC";
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String type = rs.getString("type");
                String reason = rs.getString("reason");
                String punisherUUID = rs.getString("punisher_uuid");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp completedAt = rs.getTimestamp("completed_at");
                String punisherName = getPunisherName(punisherUUID);
                String durationStr = (completedAt == null) ? "Permanent" : PunishmentManager.formatDuration(completedAt.getTime() - createdAt.getTime());
                String status = (completedAt == null || completedAt.after(new Timestamp(System.currentTimeMillis()))) ? MessageUtils.format("&aAktiv") : MessageUtils.format("&cAbgelaufen");

                String entry = MessageUtils.format("&8[&7" + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(createdAt) + "&8] &7" + type + " &8- &7" + reason + " &8- &7Von: &c" + punisherName + " &8- &7Dauer: &e" + durationStr + " &8- " + status);
                entries.add(entry);
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return entries;
    }

    private String getPunisherName(String punisherUUID) {
        if (punisherUUID.equals("CONSOLE")) {
            return "Konsole";
        }
        String name = UUIDFetcher.getNameFromUUID(UUID.fromString(punisherUUID));
        if (name != null) {
            return name;
        }
        return "Unbekannter Spieler";
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("history.use")) {
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
        }
        return suggestions;
    }
}
