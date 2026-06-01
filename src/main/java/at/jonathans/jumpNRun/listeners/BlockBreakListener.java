package at.jonathans.jumpNRun.listeners;

import at.jonathans.jumpNRun.JumpNRun;
import at.jonathans.jumpNRun.JumpSession;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBroken(BlockBreakEvent event) {
        if (!JumpNRun.getInstance().getConfig().getBoolean("protect-blocks")) {
            return;
        }

        Location blockLocation = event.getBlock().getLocation();

        Location pos1 = JumpNRun.getInstance().getConfig().getLocation("pos1");
        Location pos2 = JumpNRun.getInstance().getConfig().getLocation("pos2");

        if (pos1 == null || pos2 == null) {
            return;
        }

        if (JumpSession.isInBounds(pos1, pos2, blockLocation)) {
            event.setCancelled(true);
        }

    }
}
