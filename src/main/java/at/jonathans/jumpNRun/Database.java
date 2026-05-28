package at.jonathans.jumpNRun;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
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

    public int getHighscore(UUID uuid) {
        if (!cache.containsKey(uuid)) {
            try {
                String sql = "SELECT score FROM highscores WHERE uuid = ? LIMIT 1";
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

}
