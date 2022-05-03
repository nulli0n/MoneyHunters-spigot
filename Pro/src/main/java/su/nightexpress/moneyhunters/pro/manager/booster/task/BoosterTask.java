package su.nightexpress.moneyhunters.pro.manager.booster.task;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.config.Config;

public class BoosterTask extends AbstractTask<MoneyHunters> {

    private long count = 0;

    public BoosterTask(@NotNull MoneyHunters plugin) {
        super(plugin, 1, true);
    }

    @Override
    public void action() {
        if (Config.BOOSTERS_UPDATE_INTERVAL > 0 && this.count % Config.BOOSTERS_UPDATE_INTERVAL == 0) {
            plugin.getBoosterManager().updateBoosters();
        }
        if (Config.BOOSTERS_NOTIFY_INTERVAL > 0 && this.count % Config.BOOSTERS_NOTIFY_INTERVAL == 0) {
            plugin.getBoosterManager().notifyBooster();
        }
        if (++this.count >= Integer.MAX_VALUE) {
            this.count = 0;
        }
    }
}
