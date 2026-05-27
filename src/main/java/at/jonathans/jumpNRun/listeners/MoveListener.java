package at.jonathans.jumpNRun.listeners;

import at.jonathans.jumpNRun.JumpNRun;
import at.jonathans.jumpNRun.JumpSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private JumpNRun plugin;

    public MoveListener() {
        plugin = JumpNRun.getInstance();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getJumpSessions().containsKey(event.getPlayer())) {
            return;
        }

        JumpSession jumpSession = plugin.getJumpSessions().get(event.getPlayer());
        jumpSession.checkNextJump();

    }

}
