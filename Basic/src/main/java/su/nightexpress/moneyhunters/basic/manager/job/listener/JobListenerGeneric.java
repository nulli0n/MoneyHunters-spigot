package su.nightexpress.moneyhunters.basic.manager.job.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.MythicMobsHook;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.playerblocktracker.PlayerBlockTracker;
import su.nightexpress.moneyhunters.basic.Keys;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.manager.job.JobManager;
import su.nightexpress.moneyhunters.basic.manager.money.MoneyManager;

public class JobListenerGeneric extends AbstractListener<MoneyHunters> {

    public JobListenerGeneric(@NotNull JobManager jobManager) {
        super(jobManager.plugin());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJobFireworkDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Firework firework)) return;
        if (PDCUtil.getBoolean(firework, Keys.JOB_FIREWORK).orElse(false)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJobTypeEntityKill(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead instanceof Player) return;
        if (MoneyManager.isDevastated(dead)) return;
        if (Hooks.hasMythicMobs() && MythicMobsHook.isMythicMob(dead)) return;

        Player player = dead.getKiller();
        if (player == null) return;

        String type = dead.getType().name();
        this.plugin.getJobManager().getJobFactory().getJobsEntityKill().forEach(job -> {
            job.onJobEvent(e, player, type);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJobTypeFishing(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        //if (!e.getHook().isInOpenWater()) return;

        Entity caught = e.getCaught();
        if (caught == null) return;

        String type = caught.getType().name();
        if (caught instanceof Item item) {
            type = item.getItemStack().getType().name();
        }

        String objectiveName = type;
        this.plugin.getJobManager().getJobFactory().getJobsFishing().forEach(jobFishing -> {
            jobFishing.onJobEvent(e, e.getPlayer(), objectiveName);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJobTypeBlockHarvest(PlayerHarvestBlockEvent e) {
        Block block = e.getHarvestedBlock();
        if (PlayerBlockTracker.isTracked(block)) {
            return;
        }

        Player player = e.getPlayer();
        String type = block.getType().name();

        BlockBreakEvent event = new BlockBreakEvent(block, player);
        this.plugin.getJobManager().getJobFactory().getJobsBlockBreak().forEach(job -> {
            job.onJobEvent(event, player, type);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJobTypeBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        BlockData blockData = block.getBlockData();
        Material blockType = block.getType();
        boolean isTall = blockType == Material.BAMBOO || blockType == Material.SUGAR_CANE;

        // Do not give money for ungrowth plants.
        if (!isTall && blockData instanceof Ageable age) {
            if (age.getAge() < age.getMaximumAge()) return;
        }

        Player player = e.getPlayer();
        String type = blockType.name();
        int blockHeight = isTall ? (blockType == Material.BAMBOO ? 16 : 4) : 1;
        for (int currentHeight = 0; currentHeight < blockHeight; currentHeight++) {
            if (currentHeight > 0) {
                block = block.getRelative(BlockFace.UP);
                if (block.getType() != blockType) break;
            }

            if (PlayerBlockTracker.isTracked(block)) {
                //PlayerBlockTracker.unTrack(block);
                continue;
            }

            BlockBreakEvent event = new BlockBreakEvent(block, player);
            this.plugin.getJobManager().getJobFactory().getJobsBlockBreak().forEach(job -> {
                job.onJobEvent(event, player, type);
            });
        }
    }
}
