package su.nightexpress.moneyhunters.pro.manager.money.listener;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.api.currency.ICurrency;
import su.nightexpress.moneyhunters.pro.manager.money.MoneyManager;
import su.nightexpress.moneyhunters.pro.manager.money.task.MoneyMergeTask;

public class MoneyListenerGeneric extends AbstractListener<MoneyHunters> {

    private final MoneyManager moneyManager;

    public MoneyListenerGeneric(@NotNull MoneyManager moneyManager) {
        super(moneyManager.plugin());
        this.moneyManager = moneyManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoneyItemSpawn(ItemSpawnEvent e) {
        Item item = e.getEntity();
        ItemStack itemStack = item.getItemStack();
        ICurrency currency = MoneyManager.getMoneyCurrency(itemStack);
        if (currency == null) return;

        MoneyMergeTask.addItem(item);
        currency.playDropParticle(item.getLocation());
        item.setCustomName(ItemUtil.getItemName(itemStack));
        item.setCustomNameVisible(true);
    }

    // Support for plugins like RPGLoot
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMoneyItemInventoryClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item == null || !MoneyManager.isMoneyItem(item)) return;

        e.setCancelled(true);

        if (this.moneyManager.pickupMoney((Player) e.getWhoClicked(), item)) {
            e.setCurrentItem(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoneyItemPickup(EntityPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (!MoneyManager.isMoneyItem(item)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof Player player)) return;
        if (Hooks.isCitizensNPC(player) || !MoneyManager.isMoneyOwner(item, player)) return;

        if (this.moneyManager.pickupMoney(player, item)) {
            e.getItem().remove();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMoneyPenaltyPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (Hooks.isCitizensNPC(player)) return;
        if (player.hasPermission(Perms.BYPASS_DEATH_PENALTY)) return;
        if (!MoneyManager.isMoneyAvailable(player)) return;

        plugin.getCurrencyManager().getCurrencies().forEach(currency -> {
            if (!currency.isDeathPenaltyEnabled()) return;
            if (Rnd.get(true) >= currency.getDeathPenaltyChance()) return;

            double amountPercent = currency.getDeathPenaltyAmount();
            if (amountPercent <= 0) return;

            double balance = currency.getBalance(player);
            double amountLost = currency.round(balance * amountPercent / 100D);
            if (amountLost <= 0D) return;

            if (this.moneyManager.loseMoney(player, currency, amountLost)) {
                if (currency.isDeathPenaltyDropItem()) {
                    ItemStack item = currency.createMoney(amountLost, null, null, null);
                    e.getDrops().add(item);
                }
            }
        });
    }
}
