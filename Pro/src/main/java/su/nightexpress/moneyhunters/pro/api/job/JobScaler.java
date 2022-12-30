package su.nightexpress.moneyhunters.pro.api.job;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.moneyhunters.pro.Placeholders;

public class JobScaler extends Scaler {

    public JobScaler(@NotNull JYML cfg, @NotNull String path, @NotNull IJob<?> job) {
        super(cfg, path, Placeholders.JOB_LEVEL, job.getLevelStart(), job.getLevelMax(JobState.PRIMARY));
    }
}
