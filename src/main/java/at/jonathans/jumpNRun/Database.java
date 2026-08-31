package at.jonathans.jumpNRun;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Database {

    private HikariDataSource dataSource;
    private ConcurrentHashMap<UUID, Integer> cache;
    private LinkedHashMap<UUID, Integer> leaderboardCache;
    private Instant leaderboardCacheAge;

    public Database() {
        cache = new ConcurrentHashMap<>();
        leaderboardCacheAge = Instant.ofEpochSecond(0);

        JumpNRun plugin = JumpNRun.getInstance();

        HikariConfig hikariConfig = new HikariConfig();
        String databaseType = plugin.getConfig().getString("database.type");

        switch (databaseType.toLowerCase()) {
            case "sqlite":
                File dataBaseFile = new File(plugin.getDataFolder(), "data.db");
                hikariConfig.setJdbcUrl("jdbc:sqlite:" + dataBaseFile.getAbsolutePath());
                break;

            case "mysql":
                hikariConfig.setJdbcUrl("jdbc:mysql://" + plugin.getConfig().getString("database.credentials.url") + "/" + plugin.getConfig().getString("database.credentials.database"));
                hikariConfig.setUsername(plugin.getConfig().getString("database.credentials.username"));
                hikariConfig.setPassword(plugin.getConfig().getString("database.credentials.password"));
                break;
        }

        dataSource = new HikariDataSource(hikariConfig);

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS highscores(uuid VARCHAR(36) PRIMARY KEY, score INTEGER);");

        } catch (SQLException exception) {
            JumpNRun.getInstance().getLogger().severe("Failed to connect to database: " + exception.getMessage());
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

    }

    public void closeConnection() {
        dataSource.close();
    }

    public CompletableFuture<Boolean> brokeHighscore(Player player, int score) {
        return CompletableFuture.supplyAsync(() -> {

            UUID uuid = player.getUniqueId();

            CompletableFuture<Integer> oldHighscoreFuture = getHighscore(player);
            int oldHighscore = oldHighscoreFuture.join();

            if (oldHighscore >= score) {
                return false;
            }

            cache.put(uuid, score);

            try (Connection connection = dataSource.getConnection()) {
                if (oldHighscore == -1) {
                    String sql = "INSERT INTO highscores (uuid, score) VALUES (?, ?);";
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setString(1, uuid.toString());
                        statement.setInt(2, score);

                        statement.executeUpdate();
                    }

                } else {
                    String sql = "UPDATE highscores SET score = ? WHERE uuid = ?;";
                    try (PreparedStatement statement = connection.prepareStatement(sql);) {
                        statement.setInt(1, score);
                        statement.setString(2, uuid.toString());

                        statement.executeUpdate();
                    }
                }
            } catch (SQLException exception) {
                JumpNRun.getInstance().getLogger().severe("Failed to update highscore: " + exception.getMessage());
            }

            return true;

        });
    }

    public CompletableFuture<Integer> getHighscore(OfflinePlayer player) {
        return CompletableFuture.supplyAsync(() -> {

            if (cache.size() >= 1000) {
                cache.clear();
            }

            UUID uuid = player.getUniqueId();
            if (!cache.containsKey(uuid)) {
                String sql = "SELECT score FROM highscores WHERE uuid = ? LIMIT 1;";
                try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, uuid.toString());
                    ResultSet results = statement.executeQuery();

                    if (results.next()) {
                        cache.put(uuid, results.getInt("score"));
                    } else {
                        cache.put(uuid, -1);
                    }

                } catch (SQLException exception) {
                    JumpNRun.getInstance().getLogger().severe("Failed to fetch highscore from database: " + exception.getMessage());
                    cache.put(uuid, -1);
                }
            }

            return cache.get(uuid);

        });
    }

    public LinkedHashMap<OfflinePlayer, Integer> getLeaderboard() {
        if (Instant.now().isAfter(leaderboardCacheAge.plusSeconds(300))) {
            String sql = "SELECT uuid, score FROM highscores ORDER BY score DESC LIMIT 10;";
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                ResultSet results = statement.executeQuery(sql);

                LinkedHashMap<UUID, Integer> leaderboard = new LinkedHashMap<>();

                while (results.next()) {
                    UUID uuid = UUID.fromString(results.getString("uuid"));
                    Integer score = results.getInt("score");

                    cache.put(uuid, score);
                    leaderboard.put(uuid, score);
                }

                leaderboardCache = leaderboard;
                leaderboardCacheAge = Instant.now();

            } catch (SQLException exception) {
                JumpNRun.getInstance().getLogger().severe("Failed to fetch leaderboard from database: " + exception.getMessage());
            }
        }

        LinkedHashMap<OfflinePlayer, Integer> returnLeaderboard = new LinkedHashMap<>();

        for (UUID uuid : leaderboardCache.keySet()) {
            returnLeaderboard.put(Bukkit.getOfflinePlayer(uuid), leaderboardCache.get(uuid));
        }

        return returnLeaderboard;
    }

}
