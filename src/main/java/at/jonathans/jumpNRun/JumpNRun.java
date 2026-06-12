package at.jonathans.jumpNRun;

import at.jonathans.jumpNRun.commands.JumpNRunCommand;
import at.jonathans.jumpNRun.listeners.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.DyeColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Random;

public final class JumpNRun extends JavaPlugin {

    private static JumpNRun instance;
    private HashMap<Player, JumpSession> jumpSessions;
    private Database database;
    private boolean outdated = false;
    private final int BSTATS_PLUGIN_ID = 31776;
    private final String MODRINTH_PLUGIN_ID = "9W6kSxB3";

    @Override
    public void onEnable() {
        instance = this;
        jumpSessions = new HashMap<>();

        saveDefaultConfig();
        updateConfig();

        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(), this);
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new HungerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileListener(), this);
        getServer().getPluginManager().registerEvents(new PotionDrinkListener(), this);

        getCommand("jumpnrun").setExecutor(new JumpNRunCommand());

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PapiPlaceholders().register();
        }

        database = new Database();

        Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

        if (getConfig().getBoolean("check-updates")) {
            String newestVersion = getNewestVersion();

            if (newestVersion != null && !newestVersion.equals(getPluginMeta().getVersion())) {
                outdated = true;
                getLogger().warning(
                        String.format("There is a new version of JumpNRun available (%s), you can download it on Modrinth", newestVersion)
                );
            }
        }
    }

    @Override
    public void onDisable() {
        for (JumpSession session : jumpSessions.values()) {
            session.endSession();
        }

        database.closeConnection();
    }

    public static JumpNRun getInstance() {
        return instance;
    }

    public HashMap<Player, JumpSession> getJumpSessions() {
        return jumpSessions;
    }

    public Database getDatabase() {
        return database;
    }

    public DyeColor getRandomColour() {
        Random rng = new Random();
        DyeColor[] colours =  DyeColor.values();
        return colours[rng.nextInt(colours.length)];
    }

    public String getNewestVersion() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.modrinth.com/v2/project/" + MODRINTH_PLUGIN_ID + "/version"))
                .timeout(Duration.of(4, ChronoUnit.SECONDS))
                .GET()
                .build();

        ObjectMapper mapper = new ObjectMapper();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }

            PluginVersion[] versions = mapper.readValue(response.body(), PluginVersion[].class);

            if (versions.length > 0) {
                return versions[0].versionNumber();
            }
            return null;

        } catch (IOException | InterruptedException exception) {
            getLogger().warning("Couldn't check for updates. Error: " + exception.getMessage());
        }

        return null;
    }

    private void updateConfig() {
        FileConfiguration config = getConfig();

        int newestConfigVersion = config.getDefaults().getInt("config-version");
        int configVersion = config.getInt("config-version");

        if (newestConfigVersion <= configVersion) {
            return;
        }

        File oldConfigFile = new File(getDataFolder(), "config.yml");
        File backupConfigFile = new File(getDataFolder(), "old-config-backup.yml");
        oldConfigFile.renameTo(backupConfigFile);

        saveDefaultConfig();
        reloadConfig();
        FileConfiguration newConfig = getConfig();
        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(backupConfigFile);

        for (String key : oldConfig.getKeys(true)) {
            if (
                    key.equals("config-version") ||
                    oldConfig.isConfigurationSection(key) ||
                    (!newConfig.contains(key) && !key.startsWith("pos"))
            ) {
                continue;
            }

            newConfig.set(key, oldConfig.get(key));
        }
        saveConfig();

        getLogger().info("Updated config to the newest version");
    }

    public boolean isOutdated() {
        return outdated;
    }

    public String getModrinthId() {
        return MODRINTH_PLUGIN_ID;
    }

}
