package at.jonathans.jumpNRun.listeners;

import at.jonathans.jumpNRun.Config;
import at.jonathans.jumpNRun.JumpNRun;
import at.jonathans.jumpNRun.JumpSession;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBroken(BlockBreakEvent event) {
        Location blockLocation = event.getBlock().getLocation();

        Config config = JumpNRun.getInstance().getPluginConfig();
        Location pos1 = config.getPos1();
        Location pos2 = config.getPos2();

        if (JumpSession.isInBounds(pos1, pos2, blockLocation)) {
            event.setCancelled(true);
        }

    }
}
