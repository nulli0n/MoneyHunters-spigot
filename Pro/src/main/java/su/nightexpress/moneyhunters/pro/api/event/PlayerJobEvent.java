package su.nightexpress.moneyhunters.pro.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;

public abstract class PlayerJobEvent extends Event {

    protected Player      player;
    protected MoneyUser   user;
    protected UserJobData jobData;

    public PlayerJobEvent(@NotNull Player player, @NotNull MoneyUser user, @NotNull UserJobData jobData) {
        this.player = player;
        this.user = user;
        this.jobData = jobData;
    }

    @NotNull
    public final Player getPlayer() {
        return this.player;
    }

    @NotNull
    public final UserJobData getJobData() {
        return this.jobData;
    }

    @NotNull
    public final IJob<?> getJob() {
        return this.jobData.getJob();
    }

    @NotNull
    public final MoneyUser getUser() {
        return user;
    }
}
