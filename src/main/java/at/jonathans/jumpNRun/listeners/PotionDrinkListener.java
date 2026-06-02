package at.jonathans.jumpNRun.listeners;

import at.jonathans.jumpNRun.JumpNRun;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PotionDrinkListener implements Listener {

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.POTION) {
            return;
        }

        JumpNRun plugin = JumpNRun.getInstance();
        if (plugin.getConfig().getBoolean("disable-potions") && plugin.getJumpSessions().containsKey(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
