package su.nightexpress.moneyhunters.pro.manager.job.object;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.job.AbstractJob;
import su.nightexpress.moneyhunters.pro.api.job.JobType;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;

public class JobFishing extends AbstractJob<PlayerFishEvent> {

    public JobFishing(@NotNull MoneyHunters plugin, @NotNull JYML cfg) {
        super(plugin, cfg, JobType.FISHING);
    }

    @Override
    protected void handleEvent(@NotNull PlayerFishEvent event, @NotNull Player player, @NotNull IMoneyObjective objective,
                               @NotNull ItemStack moneyItem) {
        Entity caught = event.getCaught();
        if (caught == null) return;

        Location locHook = event.getHook().getLocation();
        Location locPlayer = player.getLocation();

        Vector vec3d = (new Vector(locPlayer.getX() - locHook.getX(), locPlayer.getY() - locHook.getY(), locPlayer.getZ() - locHook.getZ())).multiply(0.1D);
        Item drop = player.getWorld().dropItem(caught.getLocation(), moneyItem);
        drop.setVelocity(drop.getVelocity().add(vec3d));
    }
}
