package at.jonathans.jumpNRun.listeners;

import at.jonathans.jumpNRun.JumpNRun;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerListener implements Listener {

    private JumpNRun plugin;

    public HungerListener() {
        plugin = JumpNRun.getInstance();
    }

    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        if (!JumpNRun.getInstance().getConfig().getBoolean("disable-hunger")) {
            return;
        }

        if (event.getEntity() instanceof Player player && plugin.getJumpSessions().containsKey(player)) {
            event.setCancelled(true);
        }
    }

}
