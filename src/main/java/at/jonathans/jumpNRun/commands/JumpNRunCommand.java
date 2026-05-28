package at.jonathans.jumpNRun.commands;

import at.jonathans.jumpNRun.JumpNRun;
import at.jonathans.jumpNRun.JumpSession;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

        if (strings.length >= 1) {

            switch (strings[0].toLowerCase()) {
                case "pos1":
                case "pos2":
                    if (!commandSender.hasPermission("jumpnrun.admin")) {
                        commandSender.sendMessage("You don't have permission to execute that command!");
                        return true;
                    }

                    plugin.getConfig().set(strings[0].toLowerCase(), player.getLocation());
                    plugin.saveConfig();

                    commandSender.sendMessage("Set location");
                    return true;

                case "highscore":
                    Player targetPlayer;
                    if (strings.length >= 2) {
                        targetPlayer = Bukkit.getPlayer(strings[1]);
                    } else {
                        targetPlayer = player;
                    }

                    if (targetPlayer == null) {
                        commandSender.sendMessage("This player has never been on the server");
                        return true;
                    }

                    int highscore = plugin.getDatabase().getHighscore(targetPlayer);
                    if (highscore == -1) {
                        commandSender.sendMessage("This player doesn't have a highscore yet!");
                        return true;
                    }

                    commandSender.sendMessage(String.format("Highscore: %d", highscore));
                    return true;

                default:
                    break;
            }

        }

        if (plugin.getJumpSessions().containsKey(player)) {
            player.sendMessage("You already started a Jump & Run");
            return true;
        }

        new JumpSession(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

        if (strings.length == 1) {

            ArrayList<String> argumentList = new ArrayList<>();

            argumentList.add("play");
            argumentList.add("highscore");

            if ((commandSender.hasPermission("jumpnrun.admin"))) {
                argumentList.add("pos1");
                argumentList.add("pos2");
            }

            return argumentList;

        } else if (strings.length == 2 && strings[0].equalsIgnoreCase("highscore")) {
            return null;
        }

        return List.of();
    }
}
