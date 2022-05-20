package su.nightexpress.moneyhunters.pro.manager.money;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.ama.api.ArenaAPI;
import su.nightexpress.moneyhunters.pro.Keys;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.MoneyHuntersAPI;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.currency.ICurrency;
import su.nightexpress.moneyhunters.pro.api.event.PlayerMoneyGainEvent;
import su.nightexpress.moneyhunters.pro.api.event.PlayerMoneyLoseEvent;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.pro.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.pro.config.Config;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.manager.money.listener.MoneyListenerGeneric;
import su.nightexpress.moneyhunters.pro.manager.money.listener.MoneyListenerGlitch;
import su.nightexpress.moneyhunters.pro.manager.money.task.InventoryCheckTask;
import su.nightexpress.moneyhunters.pro.manager.money.task.MoneyMergeTask;

public class MoneyManager extends AbstractManager<MoneyHunters> {

    private InventoryCheckTask inventoryCheckTask;
    private MoneyMergeTask     moneyMergeTask;

    public MoneyManager(@NotNull MoneyHunters plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        this.addListener(new MoneyListenerGeneric(this));
        this.addListener(new MoneyListenerGlitch(this.plugin));

        this.inventoryCheckTask = new InventoryCheckTask(this);
        this.inventoryCheckTask.start();

        if (Config.MONEY_MERGING_ENABLED) {
            this.moneyMergeTask = new MoneyMergeTask(this.plugin);
            this.moneyMergeTask.start();
        }
    }

    @Override
    public void onShutdown() {
        if (this.inventoryCheckTask != null) {
            this.inventoryCheckTask.stop();
            this.inventoryCheckTask = null;
        }
        if (this.moneyMergeTask != null) {
            this.moneyMergeTask.stop();
            this.moneyMergeTask = null;
        }
    }

    public static boolean isMoneyAvailable(@NotNull Player player) {
        if (Hooks.isCitizensNPC(player)) return false;
        if (Config.GEN_DISABLED_WORLDS.contains(player.getWorld().getName())) return false;
        if (Config.GEN_GLITCH_IGNORE_GAME_MODES.contains(player.getGameMode().name())) return false;
        if (Config.isDisabledRegion(player)) return false;
        if (Config.GEN_HOOKS_DISABLED_ON_MOB_ARENA && ArenaAPI.getArenaManager().isPlaying(player)) return false;

        return true;
    }

    public static boolean isMoneyItem(@NotNull ItemStack item) {
        return PDCUtil.getDoubleData(item, Keys.MONEY_AMOUNT) != 0D;
    }

    public static boolean isMoneyOwner(@NotNull ItemStack item, @NotNull Player player) {
        String owner = getMoneyOwner(item);
        return owner == null || player.getName().equalsIgnoreCase(owner);
    }

    @Nullable
    public static String getMoneyOwner(@NotNull ItemStack item) {
        return PDCUtil.getStringData(item, Keys.MONEY_OWNER);
    }

    @Nullable
    public static IJob<?> getMoneyJob(@NotNull ItemStack item) {
        String jobId = PDCUtil.getStringData(item, Keys.MONEY_JOB);
        return jobId == null ? null : MoneyHuntersAPI.getJobById(jobId);
    }

    @Nullable
    public static IMoneyObjective getMoneyObjective(@NotNull ItemStack item) {
        String type = PDCUtil.getStringData(item, Keys.MONEY_OBJECTIVE);
        if (type == null) return null;

        IJob<?> job = getMoneyJob(item);
        return job == null ? null : job.getObjective(type);
    }

    @Nullable
    public static ICurrency getMoneyCurrency(@NotNull ItemStack item) {
        String currencyId = PDCUtil.getStringData(item, Keys.MONEY_CURRENCY);
        return currencyId == null ? null : MoneyHuntersAPI.getCurrency(currencyId);
    }

    public static double getMoneyAmount(@NotNull ItemStack item) {
        return PDCUtil.getDoubleData(item, Keys.MONEY_AMOUNT);
    }

    public static void devastateEntity(@NotNull Entity entity) {
        PDCUtil.setData(entity, Keys.MONEY_NO_DROP, true);
    }

    public static boolean isDevastated(@NotNull Entity entity) {
        return PDCUtil.getBooleanData(entity, Keys.MONEY_NO_DROP);
    }

    public boolean pickupMoney(@NotNull Player player, @NotNull ItemStack item) {
        ICurrency currency = MoneyManager.getMoneyCurrency(item);
        if (currency == null) return false;

        double money = MoneyManager.getMoneyAmount(item);
        boolean isLose = money < 0D;

        IJob<?> job = MoneyManager.getMoneyJob(item);
        IMoneyObjective objective = MoneyManager.getMoneyObjective(item);

        if (isLose) {
            return this.loseMoney(player, currency, money, job, objective);
        }
        else {
            return this.gainMoney(player, currency, money, job, objective);
        }
    }

    public boolean loseMoney(@NotNull Player player, @NotNull ICurrency currency, double money) {
        return this.loseMoney(player, currency, money, null, null);
    }

    public boolean loseMoney(@NotNull Player player, @NotNull ICurrency currency, double money,
                             @Nullable IJob<?> job, @Nullable IMoneyObjective objective) {
        PlayerMoneyLoseEvent event = new PlayerMoneyLoseEvent(player, money, job, objective);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        money = event.getAmount();
        currency.take(player, money);

        plugin.lang().Money_Lost
            .replace(Placeholders.GENERIC_MONEY, currency.format(money))
            .replace(Placeholders.GENERIC_BALANCE, currency.format(currency.getBalance(player)))
            .send(player);

        if (job != null && objective != null) {
            this.plugin.getJobManager().countObjective(player, -money, job, objective, ObjectiveLimitType.MONEY);
        }

        return true;
    }

    public boolean gainMoney(@NotNull Player player, @NotNull ICurrency currency, double money) {
        return this.gainMoney(player, currency, money, null, null);
    }

    public boolean gainMoney(@NotNull Player player, @NotNull ICurrency currency, double money,
                             @Nullable IJob<?> job, @Nullable IMoneyObjective objective) {
        PlayerMoneyGainEvent event = new PlayerMoneyGainEvent(player, money, job, objective);
        plugin.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        money = event.getAmount();
        currency.give(player, money);

        MoneyUser user = plugin.getUserManager().getOrLoadUser(player);
        if (user.getSettings().isSoundPickupEnabled()) {
            MessageUtil.sound(player, currency.getPickupEffectSound());
        }

        plugin.lang().Money_Pickup
            .replace(Placeholders.GENERIC_MONEY, currency.format(money))
            .replace(Placeholders.GENERIC_BALANCE, currency.format(currency.getBalance(player)))
            .send(player);

        if (job != null && objective != null) {
            this.plugin.getJobManager().countObjective(player, money, job, objective, ObjectiveLimitType.MONEY);
        }

        return true;
    }
}
