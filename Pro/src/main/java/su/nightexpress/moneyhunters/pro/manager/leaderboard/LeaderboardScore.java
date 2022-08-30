package su.nightexpress.moneyhunters.pro.manager.leaderboard;

import org.jetbrains.annotations.NotNull;

public record LeaderboardScore(String name, double score) {

    public static final LeaderboardScore EMPTY = new LeaderboardScore("-", 0D);

    public LeaderboardScore(@NotNull String name, double score) {
        this.name = name;
        this.score = score;
    }

    @NotNull
    public String getSkullOwner() {
        return this.isEmpty() ? LeaderboardConfig.genericMissingStatName : this.name();
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }
}
