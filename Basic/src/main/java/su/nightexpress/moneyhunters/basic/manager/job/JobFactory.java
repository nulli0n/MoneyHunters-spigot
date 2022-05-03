package su.nightexpress.moneyhunters.basic.manager.job;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.api.job.IJob;
import su.nightexpress.moneyhunters.basic.api.job.JobType;
import su.nightexpress.moneyhunters.basic.manager.job.object.JobBlockBreak;
import su.nightexpress.moneyhunters.basic.manager.job.object.JobEntityKill;
import su.nightexpress.moneyhunters.basic.manager.job.object.JobFishing;
import su.nightexpress.moneyhunters.basic.manager.job.object.JobMythicKill;

import java.util.HashSet;
import java.util.Set;

public class JobFactory extends AbstractManager<MoneyHunters> {

    private Set<JobBlockBreak> jobsBlockBreak;
    private Set<JobFishing>    jobsFishing;
    private Set<JobEntityKill> jobsEntityKill;
    private Set<JobMythicKill> jobsMythicKill;

    public JobFactory(@NotNull MoneyHunters plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.jobsBlockBreak = new HashSet<>();
        this.jobsFishing = new HashSet<>();
        this.jobsEntityKill = new HashSet<>();
        this.jobsMythicKill = new HashSet<>();
    }

    @Override
    protected void onShutdown() {
        this.jobsBlockBreak.clear();
        this.jobsFishing.clear();
        this.jobsEntityKill.clear();
        this.jobsMythicKill.clear();
    }

    @NotNull
    public IJob<?> createJob(@NotNull JYML cfg, @NotNull JobType jobType) {
        return switch (jobType) {
            case FISHING -> {
                JobFishing job = new JobFishing(plugin, cfg);
                this.getJobsFishing().add(job);
                yield job;
            }
            case BLOCK_BREAK -> {
                JobBlockBreak job = new JobBlockBreak(plugin, cfg);
                this.getJobsBlockBreak().add(job);
                yield job;
            }
            case KILL_ENTITY -> {
                JobEntityKill job = new JobEntityKill(plugin, cfg);
                this.getJobsEntityKill().add(job);
                yield job;
            }
            case KILL_MYTHIC -> {
                if (!Hooks.hasPlugin(Hooks.MYTHIC_MOBS)) {
                    throw new IllegalStateException("No dependency installed: " + Hooks.MYTHIC_MOBS);
                }
                JobMythicKill job = new JobMythicKill(plugin, cfg);
                this.getJobsMythicKill().add(job);
                yield job;
            }
        };
    }

    @NotNull
    public Set<JobBlockBreak> getJobsBlockBreak() {
        return jobsBlockBreak;
    }

    @NotNull
    public Set<JobFishing> getJobsFishing() {
        return jobsFishing;
    }

    @NotNull
    public Set<JobEntityKill> getJobsEntityKill() {
        return jobsEntityKill;
    }

    @NotNull
    public Set<JobMythicKill> getJobsMythicKill() {
        return jobsMythicKill;
    }
}
