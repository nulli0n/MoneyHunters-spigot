package su.nightexpress.moneyhunters.pro.api.booster;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.Constants;
import su.nightexpress.moneyhunters.pro.api.job.IJob;

import java.util.Set;

public interface IBooster extends IPlaceholder {

    @NotNull String getId();

    @NotNull Set<String> getJobs();

    default boolean isApplicable(@NotNull IJob<?> job) {
        return this.getJobs().contains(job.getId()) || this.getJobs().contains(Constants.MASK_ANY);
    }

    double getMoneyModifier();

    double getExpModifier();

    default double getMoneyPercent() {
        return this.getMoneyModifier() * 100D - 100D;
    }

    default double getExpPercent() {
        return this.getExpModifier() * 100D - 100D;
    }

    default boolean isActive() {
        return !this.isAwaiting() && !this.isExpired();
    }

    boolean isExpired();

    boolean isAwaiting();

    @NotNull BoosterType getType();
}
