package de.janoschbl.cozy.managers;

import de.janoschbl.cozy.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PointsManager {

    public static void addPoints(String uuid, int points, String reason) {
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection()) {

            try (PreparedStatement psHistory = connection.prepareStatement(
                    "INSERT INTO points_history (uuid, points, reason, timestamp) VALUES (?, ?, ?, ?)"
            )) {
                psHistory.setString(1, uuid);
                psHistory.setInt(2, points);
                psHistory.setString(3, reason);
                psHistory.setLong(4, System.currentTimeMillis());
                psHistory.executeUpdate();
            }

            int totalPoints = getTotalPoints(uuid) + points;

            try (PreparedStatement psPoints = connection.prepareStatement(
                    "INSERT INTO player_points (uuid, points, last_decay) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE points = ?"
            )) {
                psPoints.setString(1, uuid);
                psPoints.setInt(2, totalPoints);
                psPoints.setLong(3, System.currentTimeMillis());
                psPoints.setInt(4, totalPoints);
                psPoints.executeUpdate();
            }

        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
    }


    public static int getTotalPoints(String uuid) {
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT points FROM player_points WHERE uuid = ?"
             )) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int totalPoints = rs.getInt("points");
                totalPoints = applyDecay(uuid, totalPoints);
                return totalPoints;
            } else {
                return 0;
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return 0;
    }


    private static int applyDecay(String uuid, int currentPoints) {
        if (!Main.getInstance().getConfigManager().getConfig().getBoolean("points.decay.enable")) {
            return currentPoints;
        }

        long now = System.currentTimeMillis();
        long decayInterval = Main.getInstance().getConfigManager().getConfig().getLong("points.decay.interval_days") * 86400000L;

        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement psSelect = connection.prepareStatement(
                     "SELECT last_decay FROM player_points WHERE uuid = ?"
             )) {

            psSelect.setString(1, uuid);
            try (ResultSet rs = psSelect.executeQuery()) {
                long lastDecay = 0;
                if (rs.next()) {
                    lastDecay = rs.getLong("last_decay");
                }

                if (now - lastDecay >= decayInterval) {
                    int decayAmount = Main.getInstance().getConfigManager().getConfig().getInt("points.decay.amount");
                    int newPoints = Math.max(0, currentPoints - decayAmount);

                    try (PreparedStatement psUpdate = connection.prepareStatement(
                            "UPDATE player_points SET points = ?, last_decay = ? WHERE uuid = ?"
                    )) {
                        psUpdate.setInt(1, newPoints);
                        psUpdate.setLong(2, now);
                        psUpdate.setString(3, uuid);
                        psUpdate.executeUpdate();
                    }

                    return newPoints;
                } else {
                    return currentPoints;
                }
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
        return currentPoints;
    }


    public static void setPoints(String uuid, int points) {
        try (Connection connection = Main.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO player_points (uuid, points, last_decay) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE points = ?"
             )) {
            ps.setString(1, uuid);
            ps.setInt(2, points);
            ps.setLong(3, System.currentTimeMillis());
            ps.setInt(4, points);
            ps.executeUpdate();
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
    }
}
