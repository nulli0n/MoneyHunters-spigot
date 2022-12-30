package su.nightexpress.moneyhunters.pro.manager.money.object;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.moneyhunters.pro.api.money.AbstractMoneyObjective;
import su.nightexpress.moneyhunters.pro.api.money.ObjectiveLimitType;

import java.util.Map;

public class MoneyObjective extends AbstractMoneyObjective {

    public MoneyObjective(
        @NotNull String type,
        double chance,
        double moneyMin, double moneyMax,
        int expMin, int expMax,

        int unlockLevel,
        @NotNull Map<ObjectiveLimitType, Scaler> limit
    ) {
        super(type, chance, moneyMin, moneyMax, expMin, expMax, unlockLevel, limit);
    }
}
