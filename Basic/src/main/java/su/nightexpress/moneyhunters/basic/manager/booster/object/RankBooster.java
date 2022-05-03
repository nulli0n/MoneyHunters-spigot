package su.nightexpress.moneyhunters.basic.manager.booster.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.moneyhunters.basic.api.booster.AbstractBooster;
import su.nightexpress.moneyhunters.basic.api.booster.BoosterType;

import java.util.Set;
import java.util.UUID;

public class RankBooster extends AbstractBooster {

    private final int    priority;
    private final String rank;

    public RankBooster(@NotNull Set<String> jobs, double moneyModifier, double expModifier,
                       int priority, String rank) {
        super(UUID.randomUUID().toString(), jobs, moneyModifier, expModifier);
        this.priority = priority;
        this.rank = rank.toLowerCase();
    }

    @Override
    @NotNull
    public BoosterType getType() {
        return BoosterType.RANK;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isAwaiting() {
        return false;
    }

    public int getPriority() {
        return priority;
    }

    @NotNull
    public String getRank() {
        return rank;
    }
}
