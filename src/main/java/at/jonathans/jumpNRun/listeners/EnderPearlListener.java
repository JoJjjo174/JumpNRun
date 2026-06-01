package at.jonathans.jumpNRun.listeners;

import at.jonathans.jumpNRun.JumpNRun;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EnderPearlListener implements Listener {

    @EventHandler
    public void onEnderPearlUse(PlayerLaunchProjectileEvent event) {
        JumpNRun plugin = JumpNRun.getInstance();
        if (!(event.getProjectile() instanceof EnderPearl) || !plugin.getConfig().getBoolean("disable-ender-pearls")) {
            return;
        }

        if (plugin.getJumpSessions().containsKey(event.getPlayer())) {
            event.setCancelled(true);
        }

    }

}
