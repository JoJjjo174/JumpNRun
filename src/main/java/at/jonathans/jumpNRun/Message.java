package at.jonathans.jumpNRun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.w3c.dom.Text;

import java.util.LinkedHashMap;

public class Message {

    public static Component noPermissionMessage() {
        return Component.text("You don't have permission to do that!", NamedTextColor.RED);
    }

    public static Component notSetUpYet() {
        return Component.text("The plugin hasn't been set up yet, contact an administrator", NamedTextColor.RED);
    }

    public static Component brokeHighscoreMessage(int score) {
        Component component = Component.text("You reached a score of ", NamedTextColor.GRAY)
                .append(Component.text(score, NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("! Thats a new highscore!", NamedTextColor.GRAY));

        return component;
    }

    public static Component emptyLeaderboard() {
        return Component.text("No people in leaderboard", NamedTextColor.GRAY);
    }

    public static Component configReloaded() {
        return Component.text("Config reloaded", NamedTextColor.GRAY);
    }

    public static Component jumpNRunAlreadyStarted() {
        return Component.text("You already started a Jump & Run", NamedTextColor.RED);
    }

    public static Component scoreMessage(int score, int highscore) {
        Component component = Component.text("You fell! You reached a score of ", NamedTextColor.GRAY)
                .append(Component.text(score, NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(". Your highscore is ", NamedTextColor.GRAY))
                .append(Component.text(highscore, NamedTextColor.GRAY));

        return component;
    }

    public static Component highscoreMessage(OfflinePlayer player, int highscore) {
        Component component = Component.text(player.getName(), NamedTextColor.GRAY)
                .append(Component.text("'s highscore: ", NamedTextColor.GRAY))
                .append(Component.text(highscore, NamedTextColor.LIGHT_PURPLE));

        return component;
    }

    public static Component noHighscoreYet() {
        return Component.text("This player doesn't have a highscore yet", NamedTextColor.GRAY);
    }

    public static Component positionSet(String pos) {
        Component component = Component.text(pos.toLowerCase(), NamedTextColor.LIGHT_PURPLE)
                .append(Component.text(" set", NamedTextColor.GRAY));

        return component;
    }

    public static Component bossbarText(int score) {
        Component component = Component.text("Jump & Run | Score: ")
                .append(Component.text(score));

        return component;
    }

    public static Component leaderboardText(LinkedHashMap<OfflinePlayer, Integer> players) {
        Component component = Component.text("---- Leaderboard ----\n");

        int position = 1;
        for (OfflinePlayer player : players.keySet()) {
            String name = player.getName();
            int score = players.get(player);

            if (name == null) {
                name = "???";
            }

            component = component.append(Component.text(position, getLeaderboardPositionColour(position)))
                    .append(Component.text(". ", getLeaderboardPositionColour(position)))
                    .append(Component.text(name, NamedTextColor.GRAY))
                    .append(Component.text(" (", NamedTextColor.GRAY))
                    .append(Component.text(score, NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text(")\n", NamedTextColor.GRAY));

            position++;
        }

        component = component.append(Component.text("----------|----------", NamedTextColor.WHITE));

        return component;
    }

    private static TextColor getLeaderboardPositionColour(int position) {
        switch (position) {
            case 1:
                return TextColor.color(255, 215, 0);

            case 2:
                return TextColor.color(192, 192, 192);

            case 3:
                return TextColor.color(205, 127, 50);

            default:
                return NamedTextColor.DARK_GRAY;
        }
    }

}
