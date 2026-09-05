package at.jonathans.jumpNRun.commands;

import at.jonathans.jumpNRun.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
                        commandSender.sendMessage(Message.noPermissionMessage());
                        return true;
                    }

                    plugin.getConfig().set(strings[0].toLowerCase(), player.getLocation().toBlockLocation());
                    plugin.saveConfig();

                    commandSender.sendMessage(Message.positionSet(strings[0]));
                    return true;

                case "highscore":
                    OfflinePlayer targetPlayer;
                    if (strings.length >= 2) {
                        targetPlayer = Bukkit.getOfflinePlayer(strings[1]);
                    } else {
                        targetPlayer = player;
                    }

                    CompletableFuture<Integer> highscoreFuture = plugin.getDatabase().getHighscore(targetPlayer);

                    highscoreFuture.thenAccept(result -> {
                        if (result == -1) {
                            commandSender.sendMessage(Message.noHighscoreYet());
                            return;
                        }
                        commandSender.sendMessage(Message.highscoreMessage(targetPlayer, result));
                    });

                    return true;

                case "leaderboard":
                    if (plugin.getDatabase().leaderboardCacheOutdated()) {
                        commandSender.sendMessage(Message.leaderboardLoading());
                    }

                    int page;
                    if (strings.length >= 2) {
                        try {
                            page = Math.max(Integer.parseInt(strings[1]), 0) - 1;
                        } catch (NumberFormatException e) {
                            page = 0;
                        }

                    } else {
                        page = 0;
                    }
                    int amount = 10;
                    int from = page * amount;

                    CompletableFuture<ArrayList<LeaderboardEntry>> leaderboardFuture = plugin.getDatabase().getLeaderboard();

                    leaderboardFuture.thenAccept(result -> {
                        Component leaderboardComponent = Message.leaderboardText(result, from, amount);

                        if (leaderboardComponent.toString().isEmpty()) {
                            commandSender.sendMessage(Message.emptyLeaderboard());
                            return;
                        }

                        commandSender.sendMessage(leaderboardComponent);
                    });

                    return true;

                case "reload":
                    if (!commandSender.hasPermission("jumpnrun.admin")) {
                        commandSender.sendMessage(Message.noPermissionMessage());
                        return true;
                    }

                    plugin.reload();
                    commandSender.sendMessage(Message.configReloaded());
                    return true;

                default:
                    break;
            }

        }

        if (plugin.getConfig().getLocation("pos1") == null || plugin.getConfig().getLocation("pos2") == null) {
            commandSender.sendMessage(Message.notSetUpYet());
            return true;
        }

        if (plugin.getJumpSessions().containsKey(player)) {
            player.sendMessage(Message.jumpNRunAlreadyStarted());
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
            argumentList.add("leaderboard");

            if ((commandSender.hasPermission("jumpnrun.admin"))) {
                argumentList.add("pos1");
                argumentList.add("pos2");
                argumentList.add("reload");
            }

            ArrayList<String> filteredArgumentList = new ArrayList<>();

            for (String argument : argumentList) {
                if (argument.startsWith(strings[0])) {
                    filteredArgumentList.add(argument);
                }
            }

            return filteredArgumentList;

        } else if (strings.length == 2 && strings[0].equalsIgnoreCase("highscore")) {
            return null;
        }

        return List.of();
    }
}
