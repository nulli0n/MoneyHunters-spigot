package su.nightexpress.moneyhunters.basic.manager.money.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.moneyhunters.basic.api.money.AbstractMoneyObjective;

import java.util.HashMap;

public class MoneyObjective extends AbstractMoneyObjective {

    public MoneyObjective(
        @NotNull String type,
        double chance,
        double moneyMin, double moneyMax,
        int expMin, int expMax

        //int unlockLevel,
        //@NotNull Map<ObjectiveLimitType, Scaler> limit
    ) {
        super(type, chance, moneyMin, moneyMax, expMin, expMax, 0, new HashMap<>());
    }
}
