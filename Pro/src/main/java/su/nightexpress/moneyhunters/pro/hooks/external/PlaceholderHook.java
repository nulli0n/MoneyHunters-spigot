package su.nightexpress.moneyhunters.pro.hooks.external;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.hook.AbstractHook;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.booster.IBooster;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardManager;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardScore;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardType;

import java.util.Collection;

public class PlaceholderHook extends AbstractHook<MoneyHunters> {

    private MoneyExpansion moneyExpansion;

    public PlaceholderHook(@NotNull MoneyHunters plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        this.moneyExpansion = new MoneyExpansion();
        this.moneyExpansion.register();

        return true;
    }

    @Override
    public void shutdown() {
        if (this.moneyExpansion != null) {
            this.moneyExpansion.unregister();
            this.moneyExpansion = null;
        }
    }

    class MoneyExpansion extends PlaceholderExpansion {

        @Override
        @NotNull
        public String getAuthor() {
            return plugin.getAuthor();
        }

        @Override
        @NotNull
        public String getIdentifier() {
            return "moneyhunters";
        }

        @Override
        @NotNull
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) return null;

            MoneyUser user = plugin.getUserManager().getOrLoadUser(player);
            String[] split = params.split("_");
            if (split.length < 2) return null;

            String jobId = split[0];
            String key = split[1];

            IJob<?> job = plugin.getJobManager().getJobById(jobId);
            if (job != null) {
                UserJobData progress = user.getJobData(job);

                if (key.equalsIgnoreCase("level")) {
                    return String.valueOf(progress.getJobLevel());
                }

                if (key.equalsIgnoreCase("exp")) {
                    if (split.length < 3) return null;

                    String sub = split[2];
                    if (sub.equalsIgnoreCase("current")) {
                        return NumberUtil.format(progress.getJobExp());
                    }
                    if (sub.equalsIgnoreCase("max")) {
                        return NumberUtil.format(progress.getJobExpMax());
                    }
                    return null;
                }

                if (key.equalsIgnoreCase("money")) {
                    if (split.length < 3) return null;
                    String sub = split[2];

                    if (sub.equalsIgnoreCase("modifier")) {
                        return NumberUtil.format(progress.getMoneyModifier());
                    }
                }

                if (key.equalsIgnoreCase("booster")) {
                    if (split.length < 3) return null;
                    String sub = split[2];

                    Collection<IBooster> boosters = user.getBoosters(job);
                    double boostExp = boosters.stream().mapToDouble(IBooster::getExpPercent).sum();
                    double boostMoney = boosters.stream().mapToDouble(IBooster::getMoneyPercent).sum();
                    if (sub.equalsIgnoreCase("exp")) {
                        return NumberUtil.format(boostExp);
                    }
                    if (sub.equalsIgnoreCase("money")) {
                        return NumberUtil.format(boostMoney);
                    }
                }

                if (key.equalsIgnoreCase("leader")) {
                    if (split.length < 5) return null;

                    LeaderboardManager leaderboardManager = plugin.getLeaderboardManager();
                    if (leaderboardManager == null) return null;

                    LeaderboardType boardType = CollectionsUtil.getEnum(split[2].replace("-", "_"), LeaderboardType.class);
                    if (boardType == null) return null;

                    int pos = StringUtil.getInteger(split[3], -1);
                    if (pos < 1) return null;

                    LeaderboardScore score = leaderboardManager.getScore(boardType, job, pos);
                    if (score == null) return "-";

                    if (split[4].equalsIgnoreCase("name")) {
                        return score.name();
                    }
                    else if (split[4].equalsIgnoreCase("score")) {
                        return NumberUtil.format(score.score());
                    }
                    return null;
                }
            }
            return null;
        }
    }
}
