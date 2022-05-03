package su.nightexpress.moneyhunters.pro.manager.money.task;

import su.nightexpress.moneyhunters.pro.MoneyHunters;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nightexpress.moneyhunters.pro.config.Config;
import su.nightexpress.moneyhunters.pro.manager.money.MoneyManager;

public class InventoryCheckTask extends AbstractTask<MoneyHunters> {

    public InventoryCheckTask(@NotNull MoneyManager moneyManager) {
        super(moneyManager.plugin(), Config.MONEY_FULL_INVENTORY_TASK_INTERVAL, false);
    }

    @Override
    public void action() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getInventory().firstEmpty() != -1) continue;
            if (!MoneyManager.isMoneyAvailable(player)) continue;

            player.getNearbyEntities(2, 2, 2).stream().filter(e -> e instanceof Item).forEach(e -> {
                Item item = (Item) e;
                ItemStack stack = item.getItemStack();
                if (MoneyManager.isMoneyItem(stack)) {
                    EntityPickupItemEvent event = new EntityPickupItemEvent(player, item, 0);
                    plugin.getPluginManager().callEvent(event);
                }
            });
        }
    }
}
