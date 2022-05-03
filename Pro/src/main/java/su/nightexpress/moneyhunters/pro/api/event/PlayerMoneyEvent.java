package su.nightexpress.moneyhunters.pro.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;

public abstract class PlayerMoneyEvent extends Event implements Cancellable {

    protected       boolean         isCancelled;
    protected final Player          player;
    protected       double          amount;
    protected       IJob<?>         job;
    protected       IMoneyObjective objective;

    public PlayerMoneyEvent(@NotNull Player player, double amount) {
        this(player, amount, null, null);
    }

    public PlayerMoneyEvent(@NotNull Player player, double money, @Nullable IJob<?> job, @Nullable IMoneyObjective objective) {
        this.player = player;
        this.setAmount(money);
        this.setJob(job);
        this.setObjective(objective);
    }

    @Override
    public boolean isCancelled() {
        return isCancelled || this.getAmount() == 0D;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = Math.abs(amount);
    }

    public void setJob(@Nullable IJob<?> job) {
        this.job = job;
    }

    @Nullable
    public IJob<?> getJob() {
        return job;
    }

    @Nullable
    public IMoneyObjective getObjective() {
        return objective;
    }

    public void setObjective(@Nullable IMoneyObjective objective) {
        this.objective = objective;
    }
}
