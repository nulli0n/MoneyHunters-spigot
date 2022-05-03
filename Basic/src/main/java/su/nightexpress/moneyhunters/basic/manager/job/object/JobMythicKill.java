package su.nightexpress.moneyhunters.basic.manager.job.object;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.basic.api.job.JobType;
import su.nightexpress.moneyhunters.basic.api.job.AbstractJob;

public class JobMythicKill extends AbstractJob<MythicMobDeathEvent> {

    public JobMythicKill(@NotNull MoneyHunters plugin, @NotNull JYML cfg) {
        super(plugin, cfg, JobType.KILL_MYTHIC);
    }

    @Override
    protected void handleEvent(@NotNull MythicMobDeathEvent event, @NotNull Player player, @NotNull IMoneyObjective objective, @NotNull ItemStack moneyItem) {
        event.getDrops().add(moneyItem);
    }
}
