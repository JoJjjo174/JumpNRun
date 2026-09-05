package at.jonathans.jumpNRun;

import java.util.UUID;

public record LeaderboardEntry(UUID playerUuid, int highscore, int position) {
}
