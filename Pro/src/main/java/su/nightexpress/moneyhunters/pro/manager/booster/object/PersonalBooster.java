package su.nightexpress.moneyhunters.pro.manager.booster.object;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.booster.AbstractBooster;
import su.nightexpress.moneyhunters.pro.api.booster.BoosterType;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.UnaryOperator;

public class PersonalBooster extends AbstractBooster {

    private final long timeEnd;

    public PersonalBooster(@NotNull String id, @NotNull Set<String> jobs, double moneyModifier, double expModifier, int duration) {
        this(id, jobs, moneyModifier, expModifier, TimeUtil.toEpochMillis(LocalDateTime.now().plusMinutes(duration)));
    }

    public PersonalBooster(@NotNull String id, @NotNull Set<String> jobs, double moneyModifier, double expModifier, long timeEnd) {
        super(id, jobs, moneyModifier, expModifier);
        this.timeEnd = timeEnd;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> super.replacePlaceholders().apply(str
            .replace(Placeholders.BOOSTER_TIME_LEFT, TimeUtil.formatTimeLeft(this.getTimeEnd()))
        );
    }

    @Override
    @NotNull
    public BoosterType getType() {
        return BoosterType.PERSONAL;
    }

    public long getTimeEnd() {
        return this.timeEnd;
    }

    @Override
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(TimeUtil.getLocalDateTimeOf(this.getTimeEnd()));
    }

    @Override
    public boolean isAwaiting() {
        return false;
    }
}
