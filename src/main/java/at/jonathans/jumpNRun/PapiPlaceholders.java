package at.jonathans.jumpNRun;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PapiPlaceholders extends PlaceholderExpansion {

    private JumpNRun plugin;

    public PapiPlaceholders() {
        plugin = JumpNRun.getInstance();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "jumpnrun";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", JumpNRun.getInstance().getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return JumpNRun.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        switch (params.toLowerCase()) {
            case "highscore":
                return String.valueOf(plugin.getDatabase().getHighscore(player));

            case "score":
                if (!plugin.getJumpSessions().containsKey(player)) {
                    return "0";
                }

                return String.valueOf(plugin.getJumpSessions().get(player).getScore());

            case "hard_jump_chance":
                if (!plugin.getJumpSessions().containsKey(player)) {
                    return "0%";
                }

                double chance = plugin.getJumpSessions().get(player).getHardJumpChance() * 100;

                return String.format("%.0f%%", chance);

            default:
                return null;
        }

    }
}
