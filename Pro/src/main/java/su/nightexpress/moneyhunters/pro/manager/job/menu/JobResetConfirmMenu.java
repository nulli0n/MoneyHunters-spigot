package su.nightexpress.moneyhunters.pro.manager.job.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.IMenuClick;
import su.nexmedia.engine.api.menu.IMenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;

import java.util.Map;
import java.util.WeakHashMap;

public class JobResetConfirmMenu extends AbstractMenu<MoneyHunters> {

    private final Map<Player, UserJobData> cache;

    public JobResetConfirmMenu(@NotNull MoneyHunters plugin) {
        super(plugin, JYML.loadOrExtract(plugin, "/menu/job.reset.confirm.yml"), "");
        this.cache = new WeakHashMap<>();

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.CONFIRMATION_ACCEPT) {
                    UserJobData data = this.cache.get(player);
                    if (data == null) return;

                    data.reset();
                    plugin.getMessage(Lang.JOBS_RESET_SUCCESS).replace(data.replacePlaceholders()).send(player);
                    player.closeInventory();
                }
                else if (type2 == MenuItemType.CONFIRMATION_DECLINE || type2 == MenuItemType.RETURN) {
                    plugin.getJobManager().getJobListMenu().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    public void open(@NotNull Player player, @NotNull UserJobData data) {
        this.cache.put(player, data);
        this.open(player, 1);
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        UserJobData data = this.cache.get(player);
        if (data == null) return;

        ItemUtil.replace(item, data.replacePlaceholders());
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        this.cache.remove(player);
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
