package at.jonathans.jumpNRun;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class CustomConfig {

    private JumpNRun plugin;
    private File file;
    private FileConfiguration config;

    public CustomConfig(String configName) {
        plugin = JumpNRun.getInstance();

        file = new File(plugin.getDataFolder(), configName);
        if (!file.exists()) {
            plugin.saveResource(configName, false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        return config;
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

}
