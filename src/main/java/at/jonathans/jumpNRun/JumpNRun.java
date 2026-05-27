package at.jonathans.jumpNRun;

import at.jonathans.jumpNRun.commands.JumpNRunCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class JumpNRun extends JavaPlugin {

    private static JumpNRun instance;
    private HashMap<Player, JumpSession> jumpSessions;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getCommand("jumpnrun").setExecutor(new JumpNRunCommand());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static JumpNRun getInstance() {
        return instance;
    }

    public HashMap<Player, JumpSession> getJumpSessions() {
        return jumpSessions;
    }
}
