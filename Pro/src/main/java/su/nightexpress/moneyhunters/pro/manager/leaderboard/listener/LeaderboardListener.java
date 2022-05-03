package su.nightexpress.moneyhunters.pro.manager.leaderboard.listener;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.event.PlayerJobExpGainEvent;
import su.nightexpress.moneyhunters.pro.api.event.PlayerMoneyGainEvent;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardManager;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardType;

import java.util.Map;

public class LeaderboardListener extends AbstractListener<MoneyHunters> {

    private final LeaderboardManager leaderboardManager;

    public LeaderboardListener(LeaderboardManager leaderboardManager) {
        super(leaderboardManager.plugin());
        this.leaderboardManager = leaderboardManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeaderSignChange(SignChangeEvent e) {
        String head = e.getLine(0);
        if (head == null) return;
        if (!head.equalsIgnoreCase(Placeholders.SIGN_HEADER) && !head.equalsIgnoreCase(plugin.getName())) return;

        String type = e.getLine(1);
        if (type == null) return;

        LeaderboardType boardType = CollectionsUtil.getEnum(type, LeaderboardType.class);
        if (boardType == null) return;

        String jobId = e.getLine(2);
        if (jobId == null) return;

        IJob<?> job = plugin.getJobManager().getJobById(jobId);
        if (job == null) return;

        String sPos = e.getLine(3);
        if (sPos == null) return;

        int pos = StringUtil.getInteger(sPos, 0);
        if (pos <= 0) return;

        Sign sign = (Sign) e.getBlock().getState();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            leaderboardManager.addSign(sign, boardType, job, pos);
            leaderboardManager.updateSign(sign);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeaderExpGain(PlayerJobExpGainEvent e) {
        Player player = e.getPlayer();

        Map<String, Double> score = leaderboardManager.getScoresRaw(LeaderboardType.DAILY_EXP, e.getJob().getId());
        score.computeIfPresent(player.getName(), (userHas, scoreHas) -> scoreHas + (double) e.getExp());
        score.computeIfAbsent(player.getName(), s -> (double) e.getExp());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeaderMoneyGain(PlayerMoneyGainEvent e) {
        IJob<?> job = e.getJob();
        if (job == null) return;

        Player player = e.getPlayer();

        Map<String, Double> score = leaderboardManager.getScoresRaw(LeaderboardType.DAILY_MONEY, e.getJob().getId());
        score.computeIfPresent(player.getName(), (userHas, scoreHas) -> scoreHas + e.getAmount());
        score.computeIfAbsent(player.getName(), s -> e.getAmount());
    }
}
