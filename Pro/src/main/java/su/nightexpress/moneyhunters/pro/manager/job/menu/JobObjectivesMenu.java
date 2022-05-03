package su.nightexpress.moneyhunters.pro.manager.job.menu;

import io.lumine.mythic.api.mobs.MythicMob;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.MythicMobsHook;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.currency.ICurrency;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.job.JobType;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.pro.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;
import su.nightexpress.moneyhunters.pro.data.object.UserObjectiveLimit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JobObjectivesMenu extends AbstractMenuAuto<MoneyHunters, IMoneyObjective> {

    private final IJob<?>      job;
    private final List<String> objLimits;
    private final ItemStack    objLocked;
    private final ItemStack    objUnlocked;
    private final int[]        objSlots;

    public JobObjectivesMenu(@NotNull MoneyHunters plugin, @NotNull IJob<?> job) {
        super(plugin, JYML.loadOrExtract(plugin, "/menu/job.objectives.yml"), "");

        this.job = job;
        this.objLimits = StringUtil.color(cfg.getStringList("Objectives.Format.Limits"));
        this.objLocked = cfg.getItem("Objectives.Format.Locked");
        this.objUnlocked = cfg.getItem("Objectives.Format.Unlocked");
        this.objSlots = cfg.getIntArray("Objectives.Slots");

        IMenuClick click = (p, type, e) -> {
            if (!(type instanceof MenuItemType type2)) return;

            if (type2 == MenuItemType.RETURN) {
                plugin.getJobManager().getJobListMenu().open(p, 1);
            }
            else this.onItemClickDefault(p, type2);
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
    protected List<IMoneyObjective> getObjects(@NotNull Player player) {
        return new ArrayList<>(this.job.getObjectives().stream().sorted(Comparator.comparing(IMoneyObjective::getType)).toList());
    }

    @Override
    @NotNull
    protected ItemStack getObjectStack(@NotNull Player player, @NotNull IMoneyObjective objective) {
        MoneyUser user = plugin.getUserManager().getOrLoadUser(player);
        UserJobData jobData = user.getJobData(this.job);
        ICurrency currency = jobData.getJob().getCurrency();

        boolean isLocked = (jobData.getJobLevel() < objective.getUnlockLevel());

        ItemStack item = new ItemStack(isLocked ? this.objLocked : this.objUnlocked);
        if (!isLocked) this.fineMaterial(item, objective);

        boolean isLimitedExp = objective.isDailyLimited(ObjectiveLimitType.EXP, jobData.getJobLevel());
        boolean isLimitedMoney = objective.isDailyLimited(ObjectiveLimitType.MONEY, jobData.getJobLevel());
        List<String> limits = isLimitedExp || isLimitedMoney ? new ArrayList<>(this.objLimits) : Collections.emptyList();

        if (!limits.isEmpty()) {
            UserObjectiveLimit limit = jobData.getObjectiveLimit(objective.getType());

            limits.removeIf(line -> {
                if (line.contains("limit_money") && !isLimitedMoney) return true;
                if (line.contains("limit_exp") && !isLimitedExp) return true;
                return false;
            });
            limits.replaceAll(limit.replacePlaceholders(currency));
        }

        ItemUtil.replaceLore(item, "%limits%", limits);
        ItemUtil.replace(item, str -> jobData.replacePlaceholders().apply(objective.replacePlaceholders(currency, jobData.getJobLevel()).apply(str)));

        return item;
    }

    @Override
    @NotNull
    protected IMenuClick getObjectClick(@NotNull Player player, @NotNull IMoneyObjective objective) {
        return (player1, type, e) -> {

        };
    }

    @Override
    protected int[] getObjectSlots() {
        return this.objSlots;
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    private boolean setMaterial(@NotNull ItemStack item, @NotNull IMoneyObjective object) {
        Material material = Material.getMaterial(object.getType().toUpperCase());
        if (material != null) {
            if (material == Material.CARROTS) material = Material.CARROT;
            else if (material == Material.POTATOES) material = Material.POTATO;
            else if (material == Material.COCOA) material = Material.COCOA_BEANS;
            else if (material == Material.BEETROOTS) material = Material.BEETROOT;
            else if (material == Material.SWEET_BERRY_BUSH) material = Material.SWEET_BERRIES;

            item.setType(material);
            return true;
        }
        return false;
    }

    private void setEntity(@NotNull ItemStack item, @NotNull IMoneyObjective object) {
        String typeRaw = object.getType().toUpperCase();

        if (this.job.getType() == JobType.KILL_MYTHIC && Hooks.hasMythicMobs()) {
            MythicMob mythicMob = MythicMobsHook.getMobConfig(typeRaw);
            if (mythicMob != null) typeRaw = mythicMob.getEntityType();
        }

        EntityType entityType = CollectionsUtil.getEnum(typeRaw, EntityType.class);
        if (entityType == null) return;

        Material material = Material.getMaterial(entityType.name() + "_SPAWN_EGG");
        if (material != null) {
            item.setType(material);
            return;
        }

        switch (entityType) {
            case GIANT -> item.setType(Material.ZOMBIE_HEAD);
            case SNOWMAN -> item.setType(Material.PUMPKIN);
            case ILLUSIONER -> item.setType(Material.VILLAGER_SPAWN_EGG);
            case ENDER_DRAGON -> item.setType(Material.DRAGON_HEAD);
            case IRON_GOLEM -> item.setType(Material.IRON_BLOCK);
            case WITHER -> item.setType(Material.WITHER_SKELETON_SKULL);
            case MUSHROOM_COW -> item.setType(Material.RED_MUSHROOM_BLOCK);
            default -> {}
        }
    }

    private void fineMaterial(@NotNull ItemStack item, @NotNull IMoneyObjective object) {
        switch (this.job.getType()) {
            case FISHING -> {
                if (this.setMaterial(item, object)) return;
                this.setEntity(item, object);
            }
            case BLOCK_BREAK -> this.setMaterial(item, object);
            case KILL_MYTHIC, KILL_ENTITY -> this.setEntity(item, object);
            default -> {}
        }
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
