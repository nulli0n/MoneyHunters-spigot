package su.nightexpress.moneyhunters.basic.manager.money.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.playerblocktracker.PlayerBlockTracker;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.config.Config;
import su.nightexpress.moneyhunters.basic.manager.money.MoneyManager;

public class MoneyListenerGlitch extends AbstractListener<MoneyHunters> {

    public MoneyListenerGlitch(@NotNull MoneyHunters plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlitchEntitySpawn(CreatureSpawnEvent e) {
        if (Config.GEN_GLITCH_IGNORE_SPAWN_REASONS.contains(e.getSpawnReason().name())) {
            MoneyManager.devastateEntity(e.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlitchEntityTransform(EntityTransformEvent e) {
        if (MoneyManager.isDevastated(e.getEntity())) {
            e.getTransformedEntities().forEach(MoneyManager::devastateEntity);
            MoneyManager.devastateEntity(e.getTransformedEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlitchBlockGeneration(BlockFormEvent e) {
        if (Config.GEN_GLITCH_IGNORE_BLOCK_GENERATORS.contains(e.getNewState().getType().name())) {
            PlayerBlockTracker.trackForce(e.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onGlitchHopperPickup(InventoryPickupItemEvent e) {
        if (e.getInventory().getType() != InventoryType.HOPPER) return;

        ItemStack item = e.getItem().getItemStack();
        if (MoneyManager.isMoneyItem(item)) {
            e.setCancelled(true);
        }
    }
}
