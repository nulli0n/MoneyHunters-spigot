package su.nightexpress.moneyhunters.pro.manager.job.object;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.pro.api.job.JobType;
import su.nightexpress.moneyhunters.pro.api.job.AbstractJob;

public class JobMythicKill extends AbstractJob<MythicMobDeathEvent> {

    public JobMythicKill(@NotNull MoneyHunters plugin, @NotNull JYML cfg) {
        super(plugin, cfg, JobType.KILL_MYTHIC);
    }

    @Override
    protected void handleEvent(@NotNull MythicMobDeathEvent event, @NotNull Player player, @NotNull IMoneyObjective objective, @NotNull ItemStack moneyItem) {
        event.getDrops().add(moneyItem);
    }
}
