package at.jonathans.jumpNRun;

import at.jonathans.jumpNRun.commands.JumpNRunCommand;
import at.jonathans.jumpNRun.listeners.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Random;

public final class JumpNRun extends JavaPlugin {

    private static JumpNRun instance;
    private HashMap<Player, JumpSession> jumpSessions;
    private Database database;
    private final int BSTATS_PLUGIN_ID = 31776;

    @Override
    public void onEnable() {
        instance = this;
        jumpSessions = new HashMap<>();

        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new MoveListener(), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(), this);
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
}
