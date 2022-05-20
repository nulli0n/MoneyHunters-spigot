package su.nightexpress.moneyhunters.pro.data.object;

import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUser;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.moneyhunters.pro.Keys;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.MoneyHuntersAPI;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.booster.BoosterType;
import su.nightexpress.moneyhunters.pro.api.booster.IBooster;
import su.nightexpress.moneyhunters.pro.api.event.*;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.job.JobState;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.pro.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.pro.config.Config;
import su.nightexpress.moneyhunters.pro.manager.booster.BoosterManager;

import java.util.*;
import java.util.function.UnaryOperator;

public class MoneyUser extends AbstractUser<MoneyHunters> implements IPlaceholder {

    private final Map<String, UserJobData> jobData;
    private final Set<IBooster>            boosters;
    private final UserSettings settings;

    public MoneyUser(@NotNull MoneyHunters plugin, @NotNull Player player) {
        this(plugin, player.getUniqueId(), player.getName(), System.currentTimeMillis(),
            new HashMap<>(), // Job Data
            new HashSet<>(), // Personal Boosters
            new UserSettings()
        );
    }

    public MoneyUser(
        @NotNull MoneyHunters plugin,
        @NotNull UUID uuid,
        @NotNull String name,
        long lastOnline,
        @NotNull Map<String, UserJobData> jobData,
        @NotNull Set<IBooster> boosters,
        @NotNull UserSettings settings
    ) {
        super(plugin, uuid, name, lastOnline);
        this.jobData = jobData;
        this.boosters = boosters;
        this.settings = settings;

        // Remove invalid job datas.
        this.jobData.values().removeIf(Objects::isNull);

        // Update missing jobs.
        MoneyHuntersAPI.getJobs().forEach(this::getJobData);

        // Update money/exp boosters.
        this.updateBoosters();
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(Placeholders.USER_JOBS_AMOUNT_PRIMARY, String.valueOf(this.getJobsAmount(JobState.PRIMARY)))
            .replace(Placeholders.USER_JOBS_AMOUNT_SECONDARY, String.valueOf(this.getJobsAmount(JobState.SECONDARY)))
            ;
    }

    @NotNull
    public UserSettings getSettings() {
        return settings;
    }

    @NotNull
    public Map<String, UserJobData> getJobData() {
        return this.jobData;
    }

    @NotNull
    public UserJobData getJobData(@NotNull IJob<?> job) {
        return this.jobData.computeIfAbsent(job.getId(), job2 -> new UserJobData(job));
    }

    public int getJobsAmount(@NotNull JobState state) {
        return (int) this.getJobData().values().stream().filter(data -> data.getState() == state).count();
    }

    /**
     * Used to add or remove user money boosters.
     *
     * @return Modifiable collection of all non-expired money boosters.
     */
    @NotNull
    public Collection<IBooster> getBoosters() {
        return this.boosters;
    }

    @NotNull
    public Collection<IBooster> getBoosters(@NotNull IJob<?> job) {
        return this.getBoosters().stream().filter(booster -> booster.isApplicable(job)).toList();
    }

    @NotNull
    public Collection<IBooster> getBoosters(@NotNull IJob<?> job, BoosterType type) {
        return this.getBoosters(job).stream().filter(booster -> booster.getType() == type).toList();
    }

    public double getBoosterExp(@NotNull IJob<?> job) {
        return BoosterManager.getBoosterExp(this.getBoosters(job));
    }

    public double getBoosterMoney(@NotNull IJob<?> job) {
        return BoosterManager.getBoosterMoney(this.getBoosters(job));
    }

	/*public double getBoosterExp(@NotNull IJob<?> job, @NotNull BoosterType type) {
		return (this.getBoosters(job, type).stream().mapToDouble(IBooster::getExpPercent).sum() + 100D) / 100D;
	}

	public double getBoosterMoney(@NotNull IJob<?> job, @NotNull BoosterType type) {
		return (this.getBoosters(job, type).stream().mapToDouble(IBooster::getMoneyPercent).sum() + 100D) / 100D;
	}*/

    public void updateBoosters() {
        Player player = this.getPlayer();
        IBooster rankBooster = player != null ? Config.getBoosterRank(player) : null;

        this.getBoosters().removeIf(boost -> boost == null || boost.isExpired());
        this.getBoosters().removeIf(boost -> boost.getType() != BoosterType.PERSONAL && boost.getType() != BoosterType.CUSTOM);
        this.getBoosters().addAll(plugin.getBoosterManager().getBoostersAuto());
        this.getBoosters().addAll(plugin.getBoosterManager().getBoostersGlobal());

        if (rankBooster != null) {
            this.getBoosters().add(rankBooster);
        }
    }

    public void addJobLevel(@NotNull IJob<?> job, int amount) {
        if (!Config.LEVELING_ENABLED || amount == 0) return;

        UserJobData progress = this.getJobData(job);
        boolean isMinus = amount < 0;

        for (int count = 0; count < Math.abs(amount); count++) {
            int exp = isMinus ? progress.getJobExpToDown() : progress.getJobExpToUp();
            this.addJobExp(job, exp, false);
        }
    }

    public void addJobExp(@NotNull IJob<?> job, double exp) {
        this.addJobExp(job, exp, false);
    }

    public void addJobExp(@NotNull IJob<?> job, double exp, boolean useBooster) {
        this.addJobExp(job, "", exp, useBooster);
    }

    public void addJobExp(@NotNull IJob<?> job, @NotNull String source, double exp, boolean useBooster) {
        if (!Config.LEVELING_ENABLED) return;

        UserJobData jobData = this.getJobData(job);
        Player player = this.getPlayer();

        if (useBooster && exp > 0D) exp *= this.getBoosterExp(job);
        int expAdd = (int) exp;
        boolean isLose = expAdd < 0;

        if (player != null) {
            PlayerJobExpEvent event;
            if (isLose) {
                event = new PlayerJobExpLoseEvent(player, this, jobData, source, expAdd);
            }
            else {
                event = new PlayerJobExpGainEvent(player, this, jobData, source, expAdd);
            }
            plugin.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            expAdd = event.getExp();
        }

        int levelHas = jobData.getJobLevel();
        if (isLose) {
            jobData.takeExp(expAdd);
        }
        else {
            jobData.addExp(expAdd);
        }

        if (player != null) {
            // Send exp gain/lose message.
            (isLose ? plugin.lang().Jobs_Leveling_Exp_Lose : plugin.lang().Jobs_Leveling_Exp_Gain)
                .replace(jobData.replacePlaceholders())
                .replace(Placeholders.GENERIC_EXP, expAdd)
                .send(player);

            // Count objective limits.
            IMoneyObjective objective = job.getObjective(source);
            if (objective != null) {
                plugin.getJobManager().countObjective(player, (isLose ? -expAdd : expAdd), job, objective, ObjectiveLimitType.EXP);
            }

            // Call events for level up/down.
            if (levelHas > jobData.getJobLevel()) {
                PlayerJobLevelDownEvent levelDownEvent = new PlayerJobLevelDownEvent(player, this, jobData);
                plugin.getPluginManager().callEvent(levelDownEvent);

                plugin.lang().Jobs_Leveling_Level_Down.replace(jobData.replacePlaceholders()).send(player);
            }
            else if (levelHas < jobData.getJobLevel()) {
                PlayerJobLevelUpEvent levelUpEvent = new PlayerJobLevelUpEvent(player, this, jobData);
                plugin.getPluginManager().callEvent(levelUpEvent);

                plugin.lang().Jobs_Leveling_Level_Up.replace(jobData.replacePlaceholders()).send(player);
                if (Config.LEVELING_LEVEUP_FIREWORK) {
                    Firework firework = EntityUtil.spawnRandomFirework(player.getLocation());
                    PDCUtil.setData(firework, Keys.JOB_FIREWORK, true);
                }
            }
        }
    }
}
