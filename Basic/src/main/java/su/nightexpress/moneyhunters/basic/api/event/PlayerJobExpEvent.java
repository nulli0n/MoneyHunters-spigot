package su.nightexpress.moneyhunters.basic.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.moneyhunters.basic.data.object.MoneyUser;
import su.nightexpress.moneyhunters.basic.data.object.UserJobData;

public abstract class PlayerJobExpEvent extends PlayerJobEvent implements Cancellable {

    protected boolean isCancelled;
    protected String  source;
    protected int     exp;

    public PlayerJobExpEvent(@NotNull Player player, @NotNull MoneyUser user, @NotNull UserJobData jobData,
                             @NotNull String source, int exp) {
        super(player, user, jobData);
        this.setExp(exp);
        this.setSource(source);
    }

    @Override
    public boolean isCancelled() {
        return isCancelled || this.getExp() == 0;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @NotNull
    public final String getSource() {
        return source;
    }

    public final void setSource(@NotNull String source) {
        this.source = source;
    }

    public final int getExp() {
        return exp;
    }

    public final void setExp(int exp) {
        this.exp = Math.abs(exp);
    }
}
