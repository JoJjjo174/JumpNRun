package at.jonathans.jumpNRun;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class Database {

    private Connection connection;
    private HashMap<UUID, Integer> cache;

    public Database(File dataBaseFile) {
        cache = new HashMap<>();

        try {
            String url = "jdbc:sqlite:" + dataBaseFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS highscores(uuid STRING, score INT);");

        } catch (SQLException exception) {
            JumpNRun.getInstance().getLogger().severe("Failed to connect to database");
        }

    }

    public void closeConnection() {
        try {
            connection.close();

        } catch (SQLException exception) {
            JumpNRun.getInstance().getLogger().warning("Failed to close database connection");
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
            JumpNRun.getInstance().getLogger().severe("Failed to update highscore");
        }

        return true;
    }

    public int getHighscore(Player player) {
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
                JumpNRun.getInstance().getLogger().severe("Failed to fetch highscore from database");
                cache.put(uuid, -1);
            }
        }

        return cache.get(uuid);
    }

    public LinkedHashMap<OfflinePlayer, Integer> getLeaderboard() {
        try {
            String sql = "SELECT uuid, score FROM highscores ORDER BY score DESC LIMIT 10;";
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);

            LinkedHashMap<OfflinePlayer, Integer> leaderboard = new LinkedHashMap<>();

            while (results.next()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(results.getString("uuid")));
                Integer score = results.getInt("score");

                leaderboard.put(player, score);
            }

            return leaderboard;

        } catch (SQLException exception) {
            JumpNRun.getInstance().getLogger().severe("Failed to fetch leaderboard from database");
            return null;
        }

    }

}
