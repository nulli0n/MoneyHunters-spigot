package su.nightexpress.moneyhunters.basic.manager.job.object;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.LocationUtil;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.basic.api.job.JobType;
import su.nightexpress.moneyhunters.basic.api.job.AbstractJob;

public class JobBlockBreak extends AbstractJob<BlockBreakEvent> {

    public JobBlockBreak(@NotNull MoneyHunters plugin, @NotNull JYML cfg) {
        super(plugin, cfg, JobType.BLOCK_BREAK);
    }

    @Override
    protected void handleEvent(@NotNull BlockBreakEvent event, @NotNull Player player, @NotNull IMoneyObjective objective, @NotNull ItemStack moneyItem) {
        Block block = event.getBlock();
        Location location = LocationUtil.getCenter(block.getLocation(), false);
        block.getWorld().dropItem(location, moneyItem);
    }
}
