package at.jonathans.jumpNRun.commands;

import at.jonathans.jumpNRun.JumpNRun;
import at.jonathans.jumpNRun.JumpSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JumpNRunCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Command can only be executed by a player");
            return true;
        }

        JumpNRun plugin = JumpNRun.getInstance();
        Player player = (Player) commandSender;

        if (strings.length >= 1 && commandSender.hasPermission("jumpnrun.admin")) {

            if (!strings[0].equalsIgnoreCase("pos1") && !strings[0].equalsIgnoreCase("pos2")) {
                commandSender.sendMessage("Invalid Argument");
                return true;
            }

            plugin.getConfig().set(strings[0].toLowerCase(), player.getLocation());
            plugin.saveConfig();

            commandSender.sendMessage("Set location");

        } else {
            if (plugin.getJumpSessions().containsKey(player)) {
                player.sendMessage("You already started a Jump & Run");
                return true;
            }

            new JumpSession(player);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender.hasPermission("jumpnrun.admin"))) {
            return List.of();
        }

        return switch (strings.length) {
            case 1 -> List.of("pos1", "pos2");
            default -> List.of();
        };
    }
}
