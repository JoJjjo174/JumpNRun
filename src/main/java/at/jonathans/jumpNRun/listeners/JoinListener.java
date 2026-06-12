package at.jonathans.jumpNRun.listeners;

import at.jonathans.jumpNRun.JumpNRun;
import at.jonathans.jumpNRun.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private JumpNRun plugin;

    public JoinListener() {
        plugin = JumpNRun.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        if (plugin.isOutdated() && event.getPlayer().hasPermission("jumpnrun.admin")) {
            event.getPlayer().sendMessage(Message.getOutdatedMessage());
        }

    }

}
