package de.janoschbl.cozy.managers;

import de.janoschbl.cozy.Main;
import de.janoschbl.cozy.utils.MessageUtils;
import de.janoschbl.cozy.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PunishmentManager {

    // Ban Player
    public static void banPlayer(CommandSender sender, String targetName, int points, String reason, long duration) {
        String uuid = String.valueOf(UUIDFetcher.getUUID(targetName));
        if (uuid == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cSpieler nicht gefunden.")));
            return;
        }

        String punisherUUID = getPunisherUUID(sender);
        if (punisherUUID == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cFehler beim Abrufen der UUID des Bestrafenden.")));
            return;
        }

        PointsManager.addPoints(uuid, points, reason);
        int totalPoints = PointsManager.getTotalPoints(uuid);
        long newDuration = 0;

        Configuration config = Main.getInstance().getConfigManager().getConfig();

        if (config.getBoolean("points.multiplier.enable") && duration != -1)  {
            int multiplierThreshold = config.getInt("points.multiplier.threshold");
            double multiplierValue = config.getDouble("points.multiplier.value");

            if (totalPoints >= multiplierThreshold) {
                newDuration = (long) (duration *  multiplierValue);
            }
        }

        if (config.getBoolean("points.extra_ban.enable") && duration != -1) {
            int extraBanThreshold = config.getInt("points.extra_ban.threshold");
            long extraBanDuration = parseDuration(config.getString("points.extra_ban.duration"));

            if (totalPoints >= extraBanThreshold) {
                newDuration += extraBanDuration;
            }
        }

        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Timestamp completedAt = (duration == -1) ? null : new Timestamp(createdAt.getTime() + newDuration);

        addPunishment(uuid, targetName, "ban", reason, punisherUUID, createdAt, completedAt);

        String[] placeholders = {"player", "reason", "punisher", "duration", "date", "points", "totalpoints"};
        String[] values = {
                targetName,
                reason,
                getPunisherName(punisherUUID),
                formatDuration(duration),
                new SimpleDateFormat("dd.MM.yyyy HH:mm").format(createdAt),
                String.valueOf(points),
                String.valueOf(totalPoints)
        };

        String banMessage = MessageUtils.getMessage("ban-message");
        banMessage = MessageUtils.replacePlaceholders(banMessage, placeholders, values);

        ProxiedPlayer target = Main.getInstance().getProxy().getPlayer(targetName);
        if (target != null) {
            target.disconnect(new TextComponent(banMessage));
        }

        String senderMessage = MessageUtils.getMessageWithPrefix("ban-success");
        senderMessage = MessageUtils.replacePlaceholders(senderMessage, new String[]{"player", "duration", "points", "totalpoints"}, new String[]{targetName, formatDuration(duration), String.valueOf(points), String.valueOf(totalPoints)});
        sender.sendMessage(new TextComponent(senderMessage));
    }

    // Mute Player
    public static void mutePlayer(CommandSender sender, String targetName, String reason, long duration) {
        String uuid = String.valueOf(UUIDFetcher.getUUID(targetName));
        if (uuid == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cSpieler nicht gefunden.")));
            return;
        }

        String punisherUUID = getPunisherUUID(sender);
        if (punisherUUID == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cFehler beim Abrufen der UUID des Bestrafenden.")));
            return;
        }

        int points = Main.getInstance().getConfigManager().getConfig().getInt("points.punishments.mute");
        PointsManager.addPoints(uuid, points, reason);
        int totalPoints = PointsManager.getTotalPoints(uuid);

        Configuration config = Main.getInstance().getConfigManager().getConfig();

        if (config.getBoolean("points.multiplier.enable")) {
            int multiplierThreshold = config.getInt("points.multiplier.threshold");
            double multiplierValue = config.getDouble("points.multiplier.value");

            if (totalPoints >= multiplierThreshold) {
                duration *= (long) multiplierValue;
            }
        }

        if (config.getBoolean("points.extra_mute.enable")) {
            int extraMuteThreshold = config.getInt("points.extra_mute.threshold");
            long extraMuteDuration = parseDuration(config.getString("points.extra_mute.duration"));

            if (totalPoints >= extraMuteThreshold) {
                duration += extraMuteDuration;
            }
        }

        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Timestamp completedAt = (duration == -1) ? null : new Timestamp(System.currentTimeMillis() + duration);

        addPunishment(uuid, targetName, "mute", reason, punisherUUID, createdAt, completedAt);

        String[] placeholders = {"player", "reason", "punisher", "duration", "date", "points", "totalpoints"};
        String[] values = {
                targetName,
                reason,
                getPunisherName(punisherUUID),
                formatDuration(duration),
                new SimpleDateFormat("dd.MM.yyyy HH:mm").format(createdAt),
                String.valueOf(points),
                String.valueOf(totalPoints)
        };

        String muteMessage = MessageUtils.getMessage("mute-message");
        muteMessage = MessageUtils.replacePlaceholders(muteMessage, placeholders, values);

        ProxiedPlayer target = Main.getInstance().getProxy().getPlayer(targetName);
        if (target != null) {
            target.sendMessage(new TextComponent(muteMessage));
        }

        String senderMessage = MessageUtils.getMessageWithPrefix("mute-success");
        senderMessage = MessageUtils.replacePlaceholders(senderMessage, new String[]{"player", "duration", "points", "totalpoints"}, new String[]{targetName, formatDuration(duration), String.valueOf(points), String.valueOf(totalPoints)});
        sender.sendMessage(new TextComponent(senderMessage));
    }

    // Warn Player
    public static void warnPlayer(CommandSender sender, String targetName, String reason) {
        String uuid = String.valueOf(UUIDFetcher.getUUID(targetName));
        if (uuid == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cSpieler nicht gefunden.")));
            return;
        }

        String punisherUUID = getPunisherUUID(sender);
        if (punisherUUID == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cFehler beim Abrufen der UUID des Bestrafenden.")));
            return;
        }

        int points = Main.getInstance().getConfigManager().getConfig().getInt("points.punishments.warn");
        PointsManager.addPoints(uuid, points, reason);
        int totalPoints = PointsManager.getTotalPoints(uuid);

        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        addPunishment(uuid, targetName, "warn", reason, punisherUUID, createdAt, null);

        String[] placeholders = {"player", "reason", "punisher", "date", "points", "totalpoints"};
        String[] values = {
                targetName,
                reason,
                getPunisherName(punisherUUID),
                new SimpleDateFormat("dd.MM.yyyy HH:mm").format(createdAt),
                String.valueOf(points),
                String.valueOf(totalPoints)
        };

        String warnMessage = MessageUtils.getMessage("warn-message");
        warnMessage = MessageUtils.replacePlaceholders(warnMessage, placeholders, values);

        ProxiedPlayer target = Main.getInstance().getProxy().getPlayer(targetName);
        if (target != null) {
            target.sendMessage(new TextComponent(warnMessage));
        }

        String senderMessage = MessageUtils.getMessageWithPrefix("warn-success");
        senderMessage = MessageUtils.replacePlaceholders(senderMessage, new String[]{"player", "points", "totalpoints"}, new String[]{targetName, String.valueOf(points), String.valueOf(totalPoints)});
        sender.sendMessage(new TextComponent(senderMessage));
    }

    // Kick Player
    public static void kickPlayer(CommandSender sender, String targetName, String reason) {
        String uuid = String.valueOf(UUIDFetcher.getUUID(targetName));
        if (uuid == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cSpieler nicht gefunden.")));
            return;
        }

        String punisherUUID = getPunisherUUID(sender);
        if (punisherUUID == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cFehler beim Abrufen der UUID des Bestrafenden.")));
            return;
        }

        int points = Main.getInstance().getConfigManager().getConfig().getInt("points.punishments.kick");
        PointsManager.addPoints(uuid, points, reason);
        int totalPoints = PointsManager.getTotalPoints(uuid);

        Timestamp createdAt = new Timestamp(System.currentTimeMillis());

        addPunishment(uuid, targetName, "kick", reason, punisherUUID, createdAt, null);

        String[] placeholders = {"player", "reason", "punisher", "date", "points", "totalpoints"};
        String[] values = {
                targetName,
                reason,
                getPunisherName(punisherUUID),
                new SimpleDateFormat("dd.MM.yyyy HH:mm").format(createdAt),
                String.valueOf(points),
                String.valueOf(totalPoints)
        };

        String kickMessage = MessageUtils.getMessage("kick-message");
        kickMessage = MessageUtils.replacePlaceholders(kickMessage, placeholders, values);

        ProxiedPlayer target = Main.getInstance().getProxy().getPlayer(targetName);
        if (target != null) {
            target.disconnect(new TextComponent(kickMessage));
        }

        String senderMessage = MessageUtils.getMessageWithPrefix("kick-success");
        senderMessage = MessageUtils.replacePlaceholders(senderMessage, new String[]{"player", "points", "totalpoints"}, new String[]{targetName, String.valueOf(points), String.valueOf(totalPoints)});
        sender.sendMessage(new TextComponent(senderMessage));
    }


    public static void unmutePlayer(CommandSender sender, String targetName) {
        String uuid = String.valueOf(UUIDFetcher.getUUID(targetName));
        if (uuid == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cSpieler nicht gefunden.")));
            return;
        }

        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE punishments SET completed_at = NOW() WHERE uuid = ? AND type = ? AND (completed_at IS NULL OR completed_at >= NOW())"
             )) {

            ps.setString(1, uuid);
            ps.setString(2, "mute");
            ps.executeUpdate();

            String senderMessage = MessageUtils.getMessageWithPrefix("unmute-success");
            senderMessage = senderMessage.replace("%player%", targetName);
            sender.sendMessage(new TextComponent(senderMessage));
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
    }


    public static void unbanPlayer(CommandSender sender, String targetName) {
        String uuid = String.valueOf(UUIDFetcher.getUUID(targetName));
        if (uuid == null) {
            sender.sendMessage(new TextComponent(MessageUtils.formatWithPrefix("&cSpieler nicht gefunden.")));
            return;
        }

        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE punishments SET completed_at = NOW() WHERE uuid = ? AND type = ? AND (completed_at IS NULL OR completed_at >= NOW())"
             )) {

            ps.setString(1, uuid);
            ps.setString(2, "ban");
            ps.executeUpdate();

            String senderMessage = MessageUtils.getMessageWithPrefix("unban-success");
            senderMessage = senderMessage.replace("%player%", targetName);
            sender.sendMessage(new TextComponent(senderMessage));
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
    }


    public static boolean isPlayerBanned(String uuid) {
        String query = "SELECT * FROM punishments WHERE uuid = ? AND type = 'ban' AND (completed_at IS NULL OR completed_at >= NOW()) ORDER BY created_at DESC";
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return false;
    }


    public static boolean isPlayerMuted(String uuid) {
        String query = "SELECT * FROM punishments WHERE uuid = ? AND type = 'mute' AND (completed_at IS NULL OR completed_at >= NOW())";
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return false;
    }


    public static String getBanReason(String uuid) {
        String query = "SELECT * FROM punishments WHERE uuid = ? AND type = ? AND (completed_at IS NULL OR completed_at >= NOW()) ORDER BY created_at DESC LIMIT 1";
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, "ban");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String reason = rs.getString("reason");
                String punisherUUID = rs.getString("punisher_uuid");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp completedAt = rs.getTimestamp("completed_at");
                String punisherName = getPunisherName(punisherUUID);
                String durationStr = (completedAt == null) ? "Permanent" : formatDuration(completedAt.getTime() - createdAt.getTime());

                String[] placeholders = {"reason", "punisher", "date", "duration"};
                String[] values = {
                        reason,
                        punisherName,
                        new SimpleDateFormat("dd.MM.yyyy HH:mm").format(createdAt),
                        durationStr
                };

                String banFormat = MessageUtils.getMessage("ban-format");
                banFormat = MessageUtils.replacePlaceholders(banFormat, placeholders, values);

                return banFormat;
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return null;
    }


    public static String getMuteReason(String uuid) {
        String query = "SELECT * FROM punishments WHERE uuid = ? AND type = ? AND (completed_at IS NULL OR completed_at >= NOW()) ORDER BY created_at DESC LIMIT 1";
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, "mute");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String reason = rs.getString("reason");
                String punisherUUID = rs.getString("punisher_uuid");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp completedAt = rs.getTimestamp("completed_at");
                String punisherName = getPunisherName(punisherUUID);
                String durationStr = (completedAt == null) ? "Permanent" : formatDuration(completedAt.getTime() - createdAt.getTime());

                String[] placeholders = {"reason", "punisher", "date", "duration"};
                String[] values = {
                        reason,
                        punisherName,
                        new SimpleDateFormat("dd.MM.yyyy HH:mm").format(createdAt),
                        durationStr
                };

                String muteFormat = MessageUtils.getMessage("mute-format");
                muteFormat = MessageUtils.replacePlaceholders(muteFormat, placeholders, values);

                return muteFormat;
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return null;
    }


    public static List<String> getBannedPlayers() {
        List<String> bannedPlayers = new ArrayList<>();
        String query = "SELECT player_name FROM punishments WHERE type = ? AND (completed_at IS NULL OR completed_at >= NOW())";
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, "ban");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String playerName = rs.getString("player_name");
                bannedPlayers.add(playerName);
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return bannedPlayers;
    }


    public static List<String> getMutedPlayers() {
        List<String> mutedPlayers = new ArrayList<>();
        String query = "SELECT player_name FROM punishments WHERE type = ? AND (completed_at IS NULL OR completed_at >= NOW())";
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, "mute");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String playerName = rs.getString("player_name");
                mutedPlayers.add(playerName);
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return mutedPlayers;
    }


    private static void addPunishment(String uuid, String playerName, String type, String reason, String punisherUUID, Timestamp createdAt, Timestamp completedAt) {
        String insert = "INSERT INTO punishments (uuid, player_name, type, reason, punisher_uuid, created_at, completed_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, uuid);
            ps.setString(2, playerName);
            ps.setString(3, type);
            ps.setString(4, reason);
            ps.setString(5, punisherUUID);
            ps.setTimestamp(6, createdAt);
            if (completedAt != null) {
                ps.setTimestamp(7, completedAt);
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
    }


    private static String getPunisherUUID(CommandSender sender) {
        if (sender instanceof ProxiedPlayer) {
            return ((ProxiedPlayer) sender).getUniqueId().toString();
        }
        return "CONSOLE";
    }


    private static String getPunisherName(String punisherUUID) {
        if (punisherUUID.equals("CONSOLE")) {
            return "Konsole";
        }
        try(Connection connection = Main.getInstance().getDatabaseManager().getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT player_name FROM punishments WHERE uuid = ? ORDER BY created_at DESC LIMIT 1")) {
            ps.setString(1, punisherUUID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("player_name");
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return "Unbekannter Spieler";
    }


    public static long parseDuration(String durationString) {
        try {
            if (durationString.equalsIgnoreCase("Permanent")) {
                return -1;
            }
            long time = 0;
            String value = durationString.substring(0, durationString.length() - 1);
            String unit = durationString.substring(durationString.length() - 1).toLowerCase();
            int val = Integer.parseInt(value);
            time = switch (unit) {
                case "s" -> val * 1000L;
                case "m" -> val * 60 * 1000L;
                case "h" -> val * 3600 * 1000L;
                case "d" -> val * 86400 * 1000L;
                case "w" -> val * 604800 * 1000L;
                default -> -1;
            };
            return time;
        } catch (Exception e) {
            return -1;
        }
    }


    public static String formatDuration(long duration) {
        if (duration <= 0) {
            return "Permanent";
        }
        long totalSeconds = duration / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }
}
