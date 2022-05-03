package su.nightexpress.moneyhunters.basic.api.money;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.moneyhunters.basic.api.currency.ICurrency;

import java.util.function.UnaryOperator;

public interface IMoneyObjective extends IPlaceholder {

    @NotNull UnaryOperator<String> replacePlaceholders(@NotNull ICurrency currency, int jobLevel);

    @NotNull String getName();

    @NotNull String getType();

    void setType(@NotNull String type);

    double getChance();

    void setChance(double chance);

    double getMoneyMin();

    void setMoneyMin(double money);

    double getMoneyMax();

    void setMoneyMax(double money);

    default double getMoney() {
        return Rnd.getDouble(this.getMoneyMin(), this.getMoneyMax());
    }

    int getExpMin();

    void setExpMin(int expMin);

    int getExpMax();

    void setExpMax(int expMax);

    default int getExp() {
        return Rnd.get(this.getExpMin(), this.getExpMax());
    }

    int getUnlockLevel();

    void setUnlockLevel(int unlockLevel);

    double getDailyLimit(@NotNull ObjectiveLimitType type, int level);

    void setDailyLimit(@NotNull ObjectiveLimitType type, @NotNull Scaler scaler);

    default boolean isDailyLimited(@NotNull ObjectiveLimitType type, int level) {
        return this.getDailyLimit(type, level) > 0D;
    }
}
