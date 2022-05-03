package su.nightexpress.moneyhunters.pro.manager.job.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;

import java.util.ArrayList;
import java.util.List;

public class JobListMenu extends AbstractMenuAuto<MoneyHunters, UserJobData> {

    private final String       formatAvailableName;
    private final List<String> formatAvailableLore;
    private final String       formatLockedPermName;
    private final List<String> formatLockedPermLore;
    private final int[]        jobSlots;

    public JobListMenu(@NotNull MoneyHunters plugin) {
        super(plugin, JYML.loadOrExtract(plugin, "/menu/job.list.yml"), "");

        this.jobSlots = cfg.getIntArray("Job_Slots");
        this.formatAvailableName = StringUtil.color(cfg.getString("Format.Available.Name", ""));
        this.formatAvailableLore = StringUtil.color(cfg.getStringList("Format.Available.Lore"));
        this.formatLockedPermName = StringUtil.color(cfg.getString("Format.Locked_Permission.Name", ""));
        this.formatLockedPermLore = StringUtil.color(cfg.getStringList("Format.Locked_Permission.Lore"));

        IMenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
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

    @Override
    @NotNull
    protected List<UserJobData> getObjects(@NotNull Player player) {
        return new ArrayList<>(plugin.getUserManager().getOrLoadUser(player).getJobData().values());
    }

    @Override
    protected @NotNull ItemStack getObjectStack(@NotNull Player player, @NotNull UserJobData data) {
        IJob<?> job = data.getJob();
        ItemStack item = job.getIcon();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        boolean hasAccess = job.hasPermission(player);
        if (hasAccess) {
            meta.setDisplayName(this.formatAvailableName);
            meta.setLore(this.formatAvailableLore);
        }
        else {
            meta.setDisplayName(this.formatLockedPermName);
            meta.setLore(this.formatLockedPermLore);
        }
        item.setItemMeta(meta);

        MoneyUser user = plugin.getUserManager().getOrLoadUser(player);
        ItemUtil.replace(item, data.replacePlaceholders(user));

        return item;
    }

    @Override
    protected @NotNull IMenuClick getObjectClick(@NotNull Player player, @NotNull UserJobData data) {
        return (player1, type, e) -> {
            IJob<?> job = data.getJob();
            if (!job.hasPermission(player1)) {
                plugin.lang().Error_Permission_Deny.send(player1);
                return;
            }
            if (e.getClick() == ClickType.DROP) {
                plugin.getJobManager().getJobResetConfirmMenu().open(player1, data);
                return;
            }
            if (e.isRightClick()) {
                if (job.getStateAllowed().isEmpty()) {
                    plugin.lang().Jobs_State_Change_Error_Nothing.send(player1);
                    return;
                }
                plugin.getJobManager().getJobStateMenu().open(player1, data);
                return;
            }
            job.getObjectivesMenu().open(player1, 1);
        };
    }

    @Override
    protected int[] getObjectSlots() {
        return this.jobSlots;
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
