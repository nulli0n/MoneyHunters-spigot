package su.nightexpress.moneyhunters.basic.manager.booster.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.moneyhunters.basic.api.booster.BoosterType;

import java.util.Set;

public class GlobalBooster extends PersonalBooster {

    public GlobalBooster(@NotNull String id, @NotNull Set<String> jobs, double moneyModifier, double expModifier, int duration) {
        super(id, jobs, moneyModifier, expModifier, duration);
    }

    @Override
    @NotNull
    public BoosterType getType() {
        return BoosterType.GLOBAL;
    }
}
