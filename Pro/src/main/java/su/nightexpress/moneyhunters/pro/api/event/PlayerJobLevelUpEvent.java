package su.nightexpress.moneyhunters.pro.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;

public class PlayerJobLevelUpEvent extends PlayerJobEvent {

    private static final HandlerList handlerList = new HandlerList();

    public PlayerJobLevelUpEvent(@NotNull Player player, @NotNull MoneyUser user, @NotNull UserJobData data) {
        super(player, user, data);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public int getNewLevel() {
        return this.getJobData().getJobLevel();
    }
}
