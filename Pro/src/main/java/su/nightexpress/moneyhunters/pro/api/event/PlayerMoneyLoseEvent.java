package su.nightexpress.moneyhunters.pro.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;

public class PlayerMoneyLoseEvent extends PlayerMoneyEvent {

    private static final HandlerList handlerList = new HandlerList();

    public PlayerMoneyLoseEvent(@NotNull Player player, double money) {
        this(player, money, null, null);
    }

    public PlayerMoneyLoseEvent(@NotNull Player player, double money, @Nullable IJob<?> job, @Nullable IMoneyObjective objective) {
        super(player, money, job, objective);
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
}
