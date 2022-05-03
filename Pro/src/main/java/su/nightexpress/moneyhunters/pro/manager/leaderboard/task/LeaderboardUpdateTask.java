package su.nightexpress.moneyhunters.pro.manager.leaderboard.task;

import su.nexmedia.engine.api.task.AbstractTask;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardConfig;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardManager;

public class LeaderboardUpdateTask extends AbstractTask<MoneyHunters> {

    private final LeaderboardManager leaderboardManager;

    public LeaderboardUpdateTask(LeaderboardManager leaderboardManager) {
        super(leaderboardManager.plugin(), LeaderboardConfig.genericUpdateMinutes, true);
        this.leaderboardManager = leaderboardManager;
    }

    @Override
    public void action() {
        leaderboardManager.updateStats();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            leaderboardManager.updateSigns();
            if (leaderboardManager.getHologramHandler() != null) leaderboardManager.getHologramHandler().updateHolograms();
        });
    }
}
