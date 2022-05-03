package su.nightexpress.moneyhunters.basic.data.object;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.moneyhunters.basic.api.currency.ICurrency;
import su.nightexpress.moneyhunters.basic.api.money.ObjectiveLimitType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class UserObjectiveLimit {

    private final Map<ObjectiveLimitType, Double>  count;
    private final Map<ObjectiveLimitType, Boolean> notified;
    private       int                              lastDay;

    public static final String PLACEHOLDER_MONEY_EARNED = "%user_limit_money_earned%";
    public static final String PLACEHOLDER_EXP_EARNED   = "%user_limit_exp_earned%";

    public UserObjectiveLimit() {
        this(0, 0);
    }

    public UserObjectiveLimit(double money, double exp) {
        this.count = new HashMap<>();
        this.notified = new HashMap<>();
        this.setCount(ObjectiveLimitType.MONEY, money);
        this.setCount(ObjectiveLimitType.EXP, exp);
        this.validateTime();
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders(@NotNull ICurrency currency) {
        return str -> str
            .replace(PLACEHOLDER_MONEY_EARNED, currency.format(this.getCount(ObjectiveLimitType.MONEY)))
            .replace(PLACEHOLDER_EXP_EARNED, NumberUtil.format(this.getCount(ObjectiveLimitType.EXP)))
            ;
    }

    public double getCount(@NotNull ObjectiveLimitType type) {
        return this.count.getOrDefault(type, 0D);
    }

    public void setCount(@NotNull ObjectiveLimitType type, double amount) {
        this.validateTime();
        this.count.put(type, Math.max(0, amount));
    }

    public boolean isNotified(@NotNull ObjectiveLimitType type) {
        return notified.getOrDefault(type, false);
    }

    public void setNotified(@NotNull ObjectiveLimitType type, boolean notified) {
        this.notified.put(type, notified);
    }

    public void validateTime() {
        LocalDateTime dateNow = LocalDateTime.now();
        if (dateNow.getDayOfMonth() != this.lastDay) {
            this.count.clear();
            this.notified.clear();
            this.lastDay = dateNow.getDayOfMonth();
        }
    }
}
