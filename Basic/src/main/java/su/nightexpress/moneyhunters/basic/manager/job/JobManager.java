package su.nightexpress.moneyhunters.basic.manager.job;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.FileUtil;
import su.nexmedia.playerblocktracker.PlayerBlockTracker;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.MoneyHuntersAPI;
import su.nightexpress.moneyhunters.basic.api.job.IJob;
import su.nightexpress.moneyhunters.basic.api.job.JobState;
import su.nightexpress.moneyhunters.basic.api.job.JobType;
import su.nightexpress.moneyhunters.basic.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.basic.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.basic.config.Lang;
import su.nightexpress.moneyhunters.basic.data.object.MoneyUser;
import su.nightexpress.moneyhunters.basic.data.object.UserJobData;
import su.nightexpress.moneyhunters.basic.data.object.UserObjectiveLimit;
import su.nightexpress.moneyhunters.basic.manager.job.listener.JobListenerGeneric;
import su.nightexpress.moneyhunters.basic.manager.job.listener.JobListenerMythic;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

public class JobManager extends AbstractManager<MoneyHunters> {

    private JobFactory           jobFactory;
    private Map<String, IJob<?>> jobs;

    private Predicate<Block> jobBlockTracker;

    public static final String DIR_JOBS = "/jobs/";

    public JobManager(@NotNull MoneyHunters plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        this.plugin.getConfigManager().extractResources(DIR_JOBS);
        this.jobs = new HashMap<>();

        this.jobFactory = new JobFactory(this.plugin);
        this.jobFactory.setup();

        FileUtil.getFolders(plugin.getDataFolder() + DIR_JOBS).forEach(this::loadJob);
        this.plugin.info("Jobs Loaded: " + this.jobs.size());

        this.addListener(new JobListenerGeneric(this));
        if (this.getJobs().stream().anyMatch(job -> job.getType() == JobType.KILL_MYTHIC)) {
            this.addListener(new JobListenerMythic(this));
        }

        PlayerBlockTracker.BLOCK_FILTERS.add(this.jobBlockTracker = (block) -> {
            // сраный бамбук из саженца можно превратить в палку поставив сверху еще один. и ни одного ивента на это нет
            Material type = block.getType();
            if (type == Material.BAMBOO_SAPLING) type = Material.BAMBOO;

            IJob<?> job = this.getJobByType(JobType.BLOCK_BREAK, type.name());
            return job != null;
        });
    }

    @Override
    public void onShutdown() {
        PlayerBlockTracker.BLOCK_FILTERS.remove(this.jobBlockTracker);
        if (this.jobFactory != null) {
            this.jobFactory.shutdown();
            this.jobFactory = null;
        }
        if (this.jobs != null) {
            this.jobs.values().forEach(IJob::clear);
            this.jobs.clear();
            this.jobs = null;
        }
    }

    public void loadJob(@NotNull File jobDir) {
        JYML cfg = JYML.loadOrExtract(plugin, "/jobs/" + jobDir.getName() + "/" + jobDir.getName() + ".yml");
        cfg.addMissing("Enabled", true);
        cfg.saveChanges();

        if (!cfg.getBoolean("Enabled")) return;

        JobType jobType = cfg.getEnum("Type", JobType.class);
        if (jobType == null) {
            this.plugin.error("Invalid job type for '" + cfg.getFile().getName() + "' job!");
            return;
        }

        try {
            IJob<?> job = this.getJobFactory().createJob(cfg, jobType);
            this.jobs.put(job.getId(), job);
        }
        catch (Exception ex) {
            plugin.error("Could not load job '" + cfg.getFile().getName() + "': " + ex.getMessage());
            //ex.printStackTrace();
        }
    }

    @NotNull
    public JobFactory getJobFactory() {
        return jobFactory;
    }

    @Nullable
    public IJob<?> getJobByType(@NotNull JobType type, @NotNull String src) {
        return this.getJobs().stream().filter(job -> job.getType() == type && job.hasObjective(src)).findFirst().orElse(null);
    }

    @Nullable
    public IJob<?> getJobById(@NotNull String id) {
        return this.jobs.get(id.toLowerCase());
    }

    @NotNull
    public Collection<IJob<?>> getJobs() {
        return this.jobs.values();
    }

    @NotNull
    public Collection<IJob<?>> getJobs(@NotNull Player player) {
        return this.getJobs().stream().filter(job -> job.hasPermission(player)).toList();
    }

    @NotNull
    public List<String> getJobIds() {
        return new ArrayList<>(this.jobs.keySet());
    }

    @NotNull
    public List<String> getJobIds(@NotNull Player player) {
        return this.getJobs(player).stream().map(IJob::getId).toList();
    }

    public static int getJobsAmountMax(@NotNull Player player, @NotNull JobState state) {
        //if (state == JobState.INACTIVE) return -1;

        return -1;
    }

    public static int getJobsAmount(@NotNull Player player, @NotNull JobState state) {
        MoneyUser user = MoneyHuntersAPI.getPlayerData(player);
        return user.getJobsAmount(state);
    }

    public void countObjective(@NotNull Player player, double amount,
                               @NotNull IJob<?> job, @NotNull IMoneyObjective objective, @NotNull ObjectiveLimitType limitType) {
        if (job.hasObjectiveLimitBypass(player, limitType)) return;

        MoneyUser user = plugin.getUserManager().getUserData(player);
        UserJobData jobData = user.getJobData(job);
        boolean isLose = amount < 0D;

        UserObjectiveLimit limit = jobData.getObjectiveLimit(objective.getType());
        limit.setCount(limitType, limit.getCount(limitType) + amount);

        if (jobData.isObjectWasted(objective, limitType) && !limit.isNotified(limitType)) {
            plugin.getMessage(Lang.JOB_OBJECTIVES_LIMITS_NOTIFY)
                .replace(jobData.replacePlaceholders())
                .replace(objective.replacePlaceholders(job.getCurrency(), jobData.getJobLevel()))
                .replace("%limit_type%", plugin.getLangManager().getEnum(limitType))
                .send(player);
            limit.setNotified(limitType, true);
        }
    }
}
