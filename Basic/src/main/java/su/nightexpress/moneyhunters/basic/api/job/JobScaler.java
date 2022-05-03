package su.nightexpress.moneyhunters.basic.api.job;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nightexpress.moneyhunters.basic.Placeholders;

public class JobScaler extends Scaler {

    public JobScaler(@NotNull JYML cfg, @NotNull String path, @NotNull IJob<?> job) {
        super(cfg, path, Placeholders.JOB_LEVEL, job.getLevelStart(), job.getLevelMax(JobState.PRIMARY));
    }
}
