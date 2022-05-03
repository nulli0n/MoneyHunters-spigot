package su.nightexpress.moneyhunters.basic.manager.booster.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.config.Config;

public class BoosterListenerGeneric extends AbstractListener<MoneyHunters> {

    public BoosterListenerGeneric(@NotNull MoneyHunters plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBoosterJoin(PlayerJoinEvent e) {
        if (!Config.BOOSTERS_NOTIFY_ON_JOIN) return;

        plugin.getBoosterManager().notifyBooster(e.getPlayer());
    }
}
