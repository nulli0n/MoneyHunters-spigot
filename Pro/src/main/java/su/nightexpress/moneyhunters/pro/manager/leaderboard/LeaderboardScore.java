package su.nightexpress.moneyhunters.pro.manager.leaderboard;

import org.jetbrains.annotations.NotNull;

public record LeaderboardScore(String name, double score) {

    public LeaderboardScore(@NotNull String name, double score) {
        this.name = name;
        this.score = score;
    }
}
