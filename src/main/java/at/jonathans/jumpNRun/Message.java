package at.jonathans.jumpNRun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.w3c.dom.Text;

import java.util.LinkedHashMap;

public class Message {

    public static Component noPermissionMessage() {
        String message = JumpNRun.getInstance().getMessages().get().getString("no-permission");

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component notSetUpYet() {
        String message = JumpNRun.getInstance().getMessages().get().getString("broke-highscore");

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component brokeHighscoreMessage(int score) {
        String message = JumpNRun.getInstance().getMessages().get().getString("not-set-up-yet");

        return MiniMessage.miniMessage().deserialize(message,
                Placeholder.component("score", Component.text(score))
        );
    }

    public static Component emptyLeaderboard() {
        String message = JumpNRun.getInstance().getMessages().get().getString("empty-leaderboard");

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component configReloaded() {
        String message = JumpNRun.getInstance().getMessages().get().getString("config-reloaded");

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component jumpNRunAlreadyStarted() {
        String message = JumpNRun.getInstance().getMessages().get().getString("jumpnrun-already-started");

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component scoreMessage(int score, int highscore) {
        String message = JumpNRun.getInstance().getMessages().get().getString("score-message");

        return MiniMessage.miniMessage().deserialize(message,
                Placeholder.component("score", Component.text(score)),
                Placeholder.component("highscore", Component.text(highscore))
        );
    }

    public static Component highscoreMessage(OfflinePlayer player, int highscore) {
        String message = JumpNRun.getInstance().getMessages().get().getString("highscore-message");

        return MiniMessage.miniMessage().deserialize(message,
                Placeholder.component("player", Component.text(player.getName())),
                Placeholder.component("highscore", Component.text(highscore))
        );
    }

    public static Component noHighscoreYet() {
        String message = JumpNRun.getInstance().getMessages().get().getString("no-highscore-yet");

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component positionSet(String pos) {
        String message = JumpNRun.getInstance().getMessages().get().getString("position-set");

        return MiniMessage.miniMessage().deserialize(message,
                Placeholder.component("pos", Component.text(pos))
        );
    }

    public static Component bossbarText(int score) {
        String message = JumpNRun.getInstance().getMessages().get().getString("bossbar-text");

        return MiniMessage.miniMessage().deserialize(message,
                Placeholder.component("score", Component.text(score))
        );
    }

    public static Component getOutdatedMessage() {
        String message = JumpNRun.getInstance().getMessages().get().getString("outdated-message");

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component leaderboardText(LinkedHashMap<OfflinePlayer, Integer> players) {
        FileConfiguration langConf = JumpNRun.getInstance().getMessages().get();

        Component component = MiniMessage.miniMessage().deserialize(langConf.getString("leaderboard.header") + "\n");

        int position = 1;
        for (OfflinePlayer player : players.keySet()) {
            String name = player.getName();
            int score = players.get(player);

            if (name == null) {
                name = "???";
            }

            component = component.append(MiniMessage.miniMessage().deserialize(langConf.getString("leaderboard.entry")+"\n",
                    Placeholder.component("position", Component.text(position, getLeaderboardPositionColour(position))),
                    Placeholder.component("score", Component.text(score)),
                    Placeholder.component("player", Component.text(name))
            ));

            position++;
        }

        component = component.append(MiniMessage.miniMessage().deserialize(langConf.getString("leaderboard.footer")));

        return component;
    }

    // --------------------------------------------------------------------------------------------------------------------

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
