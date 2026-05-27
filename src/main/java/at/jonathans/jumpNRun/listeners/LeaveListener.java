package at.jonathans.jumpNRun.listeners;

import at.jonathans.jumpNRun.JumpNRun;
import at.jonathans.jumpNRun.JumpSession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {

    private JumpNRun plugin;

    public LeaveListener() {
        plugin = JumpNRun.getInstance();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (!plugin.getJumpSessions().containsKey(event.getPlayer())) {
            return;
        }

        JumpSession jumpSession = plugin.getJumpSessions().get(event.getPlayer());
        jumpSession.endSession();
        plugin.getJumpSessions().remove(event.getPlayer());
    }

}
