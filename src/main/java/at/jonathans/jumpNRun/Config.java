package at.jonathans.jumpNRun;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private FileConfiguration config;

    private Location pos1;
    private Location pos2;
    private boolean disableHunger;
    private boolean manipulateBlocks;

    public Config() {
        config = JumpNRun.getInstance().getConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        pos1 = config.getLocation("pos1");
        pos2 = config.getLocation("pos2");
        disableHunger = config.getBoolean("disable-hunger");
        manipulateBlocks = config.getBoolean("manipulate-blocks");
    }

    public boolean hungerDisabled() {
        return disableHunger;
    }

    public boolean blockManipulationAllowed() {
        return manipulateBlocks;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

}
