package su.nightexpress.moneyhunters.pro.manager.job.listener;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.manager.job.JobManager;
import su.nightexpress.moneyhunters.pro.manager.money.MoneyManager;

public class JobListenerMythic extends AbstractListener<MoneyHunters> {

    public JobListenerMythic(@NotNull JobManager jobManager) {
        super(jobManager.plugin());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJobMythicKill(MythicMobDeathEvent e) {
        LivingEntity killer = e.getKiller();

        if (!(killer instanceof Player player)) return;
        if (MoneyManager.isDevastated(e.getEntity())) return;

        MythicMob mythicMob = e.getMobType();
        String type = mythicMob.getInternalName();

        this.plugin.getJobManager().getJobFactory().getJobsMythicKill().forEach(job -> {
            job.onJobEvent(e, player, type);
        });
    }
}
