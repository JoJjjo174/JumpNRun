package at.jonathans.jumpNRun;

import org.bukkit.plugin.java.JavaPlugin;

public final class JumpNRun extends JavaPlugin {

    private JumpNRun instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public JumpNRun getInstance() {
        return instance;
    }
}
