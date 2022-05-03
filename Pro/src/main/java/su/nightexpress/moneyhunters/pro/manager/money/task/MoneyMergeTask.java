package su.nightexpress.moneyhunters.pro.manager.money.task;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.currency.ICurrency;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.pro.manager.money.MoneyManager;

import java.util.HashSet;
import java.util.Set;

public class MoneyMergeTask extends AbstractTask<MoneyHunters> {

    private static final Set<Item> MERGE = new HashSet<>();

    public MoneyMergeTask(@NotNull MoneyHunters plugin) {
        super(plugin, 2, false);
    }

    @Override
    public void stop() {
        super.stop();
        MERGE.clear();
    }

    public static void addItem(@NotNull Item item) {
        MERGE.add(item);
    }

    @Override
    public void action() {
        MERGE.removeIf(item -> !item.isValid() || item.isDead());
        MERGE.stream().filter(Item::isOnGround).forEach(item -> {
            if (!item.isValid() || item.isDead()) return; // This is needed to due 'near' item removal.

            ItemStack stackSrc = item.getItemStack();
            ICurrency currencySrc = MoneyManager.getMoneyCurrency(stackSrc);
            if (currencySrc == null) {
                item.remove();
                return;
            }

            String ownerSrc = MoneyManager.getMoneyOwner(stackSrc);
            IJob<?> jobSrc = MoneyManager.getMoneyJob(stackSrc);
            IMoneyObjective objectiveSrc = MoneyManager.getMoneyObjective(stackSrc);
            double moneySrc = MoneyManager.getMoneyAmount(stackSrc);

            for (Entity near : item.getNearbyEntities(5, 1, 5)) {
                if (!(near instanceof Item nearItem)) continue;

                ItemStack stackNear = nearItem.getItemStack();
                if (!MoneyManager.isMoneyItem(stackNear)) continue;

                String ownerNear = MoneyManager.getMoneyOwner(stackNear);
                if (ownerNear != null && !ownerNear.equalsIgnoreCase(ownerSrc)) continue;

                IJob<?> jobNear = MoneyManager.getMoneyJob(stackNear);
                if (jobNear != jobSrc) continue; // Compare memory addresses cuz jobs are not supposed to be cloned.

                IMoneyObjective objectiveNear = MoneyManager.getMoneyObjective(stackNear);
                if (objectiveSrc != objectiveNear) continue;

                ICurrency currencyNear = MoneyManager.getMoneyCurrency(stackNear);
                if (currencyNear != currencySrc)
                    continue; // Compare memory addresses cuz currencies are not supposed to be cloned.

                moneySrc += MoneyManager.getMoneyAmount(stackNear);
                near.remove();
            }

            Player player = (ownerSrc != null) ? plugin.getServer().getPlayer(ownerSrc) : null;
            ItemStack money = currencySrc.createMoney(moneySrc, player, jobSrc, objectiveSrc);
            item.setCustomName(ItemUtil.getItemName(money));
            item.setItemStack(money);
        });
    }
}
