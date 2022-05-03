package su.nightexpress.moneyhunters.pro.manager.leaderboard;

import java.time.LocalDate;

public enum LeaderboardType {
    TOP_LEVEL,
    DAILY_EXP,
    DAILY_MONEY,
    ;

    LeaderboardType() {
        this.setLastDay();
    }

    private int lastDay;

    public int getLastDay() {
        return lastDay;
    }

    public void setLastDay() {
        this.lastDay = LocalDate.now().getDayOfWeek().getValue();
    }

    public boolean nextDay() {
        return this.lastDay != LocalDate.now().getDayOfWeek().getValue();
    }
}
