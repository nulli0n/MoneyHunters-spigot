package su.nightexpress.moneyhunters.basic.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.moneyhunters.basic.data.object.MoneyUser;
import su.nightexpress.moneyhunters.basic.data.object.UserJobData;

public class PlayerJobExpGainEvent extends PlayerJobExpEvent {

    private static final HandlerList handlerList = new HandlerList();

    public PlayerJobExpGainEvent(@NotNull Player player, @NotNull MoneyUser user, @NotNull UserJobData data,
                                 @NotNull String source, int exp) {
        super(player, user, data, source, exp);
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
