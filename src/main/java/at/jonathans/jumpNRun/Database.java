package at.jonathans.jumpNRun;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class Database {

    private Connection connection;
    private HashMap<UUID, Integer> cache;
    private LinkedHashMap<UUID, Integer> leaderboardCache;
    private Instant leaderboardCacheAge;

    public Database() {
        cache = new HashMap<>();
        leaderboardCacheAge = Instant.ofEpochSecond(0);

        JumpNRun plugin = JumpNRun.getInstance();

        try {
            String databaseType = plugin.getConfig().getString("database.type");
            if (databaseType.equalsIgnoreCase("sqlite")) {
                File dataBaseFile = new File(plugin.getDataFolder(), "data.db");
                String url = "jdbc:sqlite:" + dataBaseFile.getAbsolutePath();
                connection = DriverManager.getConnection(url);

            } else {
                String url = "jdbc:mysql://" + plugin.getConfig().getString("database.credentials.url") + "/" + plugin.getConfig().getString("database.credentials.database");
                String username = plugin.getConfig().getString("database.credentials.username");
                String password = plugin.getConfig().getString("database.credentials.password");

                connection = DriverManager.getConnection(url, username, password);
            }

            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS highscores(uuid VARCHAR(36) PRIMARY KEY, score INTEGER);");

        } catch (SQLException exception) {
            JumpNRun.getInstance().getLogger().severe("Failed to connect to database: " + exception.getMessage());
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

    }

    public void closeConnection() {
        try {
            connection.close();

        } catch (SQLException exception) {
            JumpNRun.getInstance().getLogger().warning("Failed to close database connection: " + exception.getMessage());
        }
    }

    public boolean brokeHighscore(Player player, int score) {
        UUID uuid = player.getUniqueId();
        int oldHighscore = getHighscore(player);
        if (oldHighscore >= score) {
            return false;
        }

        cache.put(uuid, score);

        try {
            if (oldHighscore == -1) {
                String sql = "INSERT INTO highscores (uuid, score) VALUES (?, ?);";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, uuid.toString());
                statement.setInt(2, score);

                statement.executeUpdate();
            } else {
                String sql = "UPDATE highscores SET score = ? WHERE uuid = ?;";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, score);
                statement.setString(2, uuid.toString());

                statement.executeUpdate();
            }

        } catch (SQLException exception) {
            JumpNRun.getInstance().getLogger().severe("Failed to update highscore: " + exception.getMessage());
        }

        return true;
    }

    public int getHighscore(OfflinePlayer player) {
        if (cache.size() >= 1000) {
            cache.clear();
        }

        UUID uuid = player.getUniqueId();
        if (!cache.containsKey(uuid)) {
            try {
                String sql = "SELECT score FROM highscores WHERE uuid = ? LIMIT 1;";
                PreparedStatement statement = connection.prepareStatement(sql);
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
    }

    public LinkedHashMap<OfflinePlayer, Integer> getLeaderboard() {
        if (Instant.now().isAfter(leaderboardCacheAge.plusSeconds(300))) {
            try {
                String sql = "SELECT uuid, score FROM highscores ORDER BY score DESC LIMIT 10;";
                Statement statement = connection.createStatement();
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
