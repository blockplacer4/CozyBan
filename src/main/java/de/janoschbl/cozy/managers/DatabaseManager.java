package de.janoschbl.cozy.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.janoschbl.cozy.Main;
import net.md_5.bungee.config.Configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private HikariDataSource dataSource;

    public void connect() {
        HikariConfig kariConfig = new HikariConfig();
        Configuration config = Main.getInstance().getConfigManager().getConfig();
        String host = config.getString("mysql.host");
        int port = config.getInt("mysql.port");
        String database = config.getString("mysql.database");
        String username = config.getString("mysql.username");
        String password = config.getString("mysql.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";

        kariConfig.setJdbcUrl(url);
        kariConfig.setUsername(username);
        kariConfig.setPassword(password);
        kariConfig.setMinimumIdle(1);

        dataSource = new HikariDataSource(kariConfig);
        createTables();
    }

    private void createTables() {
        String createPunishmentsTable = "CREATE TABLE IF NOT EXISTS punishments (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "uuid VARCHAR(36) NOT NULL," +
                "player_name VARCHAR(255) NOT NULL," +
                "type VARCHAR(10) NOT NULL," +
                "reason VARCHAR(255) NOT NULL," +
                "punisher_uuid VARCHAR(36) NOT NULL," +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "completed_at TIMESTAMP NULL DEFAULT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String createPlayerPointsTable = "CREATE TABLE IF NOT EXISTS player_points (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "points INT NOT NULL," +
                "last_decay BIGINT NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String createPointsHistoryTable = "CREATE TABLE IF NOT EXISTS points_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "uuid VARCHAR(36) NOT NULL," +
                "points INT NOT NULL," +
                "reason VARCHAR(255) NOT NULL," +
                "timestamp BIGINT NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createPunishmentsTable);
            stmt.executeUpdate(createPlayerPointsTable);
            stmt.executeUpdate(createPointsHistoryTable);
        } catch (SQLException e) {
            Main.getInstance().getLogger().warning(e.toString());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
