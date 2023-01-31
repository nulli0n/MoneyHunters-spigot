package su.nightexpress.moneyhunters.basic.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.moneyhunters.basic.api.currency.ICurrency;
import su.nightexpress.moneyhunters.basic.api.job.IJob;
import su.nightexpress.moneyhunters.basic.api.money.IMoneyObjective;

public class PlayerMoneyLoseEvent extends PlayerMoneyEvent {

    private static final HandlerList handlerList = new HandlerList();

    private ICurrency currency;

    public PlayerMoneyLoseEvent(@NotNull Player player, @NotNull ICurrency currency, double money) {
        this(player, currency, money, null, null);
    }

    public PlayerMoneyLoseEvent(@NotNull Player player, @NotNull ICurrency currency, double money, @Nullable IJob<?> job, @Nullable IMoneyObjective objective) {
        super(player, money, job, objective);
        this.setCurrency(currency);
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @NotNull
    public ICurrency getCurrency() {
        return currency;
    }

    public void setCurrency(@NotNull ICurrency currency) {
        this.currency = currency;
    }
}
