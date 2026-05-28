package at.jonathans.jumpNRun;

import at.jonathans.jumpNRun.commands.JumpNRunCommand;
import at.jonathans.jumpNRun.listeners.DeathListener;
import at.jonathans.jumpNRun.listeners.LeaveListener;
import at.jonathans.jumpNRun.listeners.MoveListener;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Random;

public final class JumpNRun extends JavaPlugin {

    private static JumpNRun instance;
    private HashMap<Player, JumpSession> jumpSessions;

    @Override
    public void onEnable() {
        instance = this;
        jumpSessions = new HashMap<>();

        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

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

    public DyeColor getRandomColour() {
        Random rng = new Random();
        DyeColor[] colours =  DyeColor.values();
        return colours[rng.nextInt(colours.length)];
    }
}
