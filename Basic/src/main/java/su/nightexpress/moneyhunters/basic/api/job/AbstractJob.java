package su.nightexpress.moneyhunters.basic.api.job;

import com.google.common.collect.Sets;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.utils.Evaluator;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.Scaler;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.Perms;
import su.nightexpress.moneyhunters.basic.Placeholders;
import su.nightexpress.moneyhunters.basic.api.currency.ICurrency;
import su.nightexpress.moneyhunters.basic.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.basic.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.basic.config.Config;
import su.nightexpress.moneyhunters.basic.data.object.MoneyUser;
import su.nightexpress.moneyhunters.basic.data.object.UserJobData;
import su.nightexpress.moneyhunters.basic.manager.currency.CurrencyId;
import su.nightexpress.moneyhunters.basic.manager.money.MoneyManager;
import su.nightexpress.moneyhunters.basic.manager.money.object.MoneyObjective;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class AbstractJob<E extends Event> extends AbstractLoadableItem<MoneyHunters> implements IJob<E> {

    protected JobType      type;
    protected ICurrency    currency;
    protected String       name;
    protected boolean      isPermissionRequired;
    protected List<String> description;
    protected ItemStack    icon;

    protected JobState      stateDefault;
    protected Set<JobState> stateAllowed;

    protected int                       levelStart;
    protected Map<JobState, Integer>    levelMax;
    protected int                       levelExpStart;
    protected TreeMap<Integer, Integer> levelExpMap;

    protected Map<JobState, JobScaler> moneyMultiplier;

    protected Map<String, IMoneyObjective> objectives;

    public AbstractJob(@NotNull MoneyHunters plugin, @NotNull JYML cfg, @NotNull JobType jobType) {
        super(plugin, cfg);

        // ------------------- UPDATE CONFIGURATION ------------------- //
        //cfg.addMissing("State.Default", JobState.PRIMARY.name());
        //cfg.addMissing("State.Allowed", Stream.of(JobState.values()).toList());

        if (!cfg.isConfigurationSection("Leveling.Money_Multiplier")) {
            int oldLevelMax = cfg.getInt("Leveling.Max_Level", -1);
            String oldMoneyMultiplier = cfg.getString("Leveling.Money_Multiplier", "1");
            for (JobState state : JobState.values()) {
                cfg.addMissing("Leveling.Max_Level." + state.name(), oldLevelMax);
                cfg.addMissing("Leveling.Money_Multiplier." + state.name(), oldMoneyMultiplier);
            }
            if (oldLevelMax != -1) {
                cfg.remove("Leveling.Max_Level");
                cfg.remove("Leveling.Money_Multiplier");
            }
        }
        cfg.saveChanges();
        // ----------------- END UPDATE CONFIGURATION ----------------- //

        this.type = jobType;
        this.name = StringUtil.color(cfg.getString("Name", this.getId()));
        this.currency = plugin.getCurrencyManager().getCurrency(cfg.getString("Currency", CurrencyId.VAULT));
        if (this.currency == null) {
            throw new IllegalArgumentException("Invalid currency provided!");
        }

        this.isPermissionRequired = cfg.getBoolean("Permission_Required");
        this.description = StringUtil.color(cfg.getStringList("Description"));
        this.icon = cfg.getItem("Icon");
        ItemUtil.replace(this.icon, this.replacePlaceholders());

        this.stateDefault = JobState.PRIMARY;
        this.stateAllowed = Sets.newHashSet(JobState.PRIMARY);

        this.levelStart = 1;
        this.levelMax = new HashMap<>();

        for (JobState state : JobState.values()) {
            this.levelMax.put(state, cfg.getInt("Leveling.Max_Level." + state.name()));
        }
        this.levelExpStart = cfg.getInt("Leveling.Start_Exp");
        this.levelExpMap = new TreeMap<>();

        if (Config.LEVELING_ENABLED) {
            String expFormula = cfg.getString("Leveling.Exp_Formula", "");
            for (int level = this.getLevelStart(); level < (this.getLevelMax(JobState.PRIMARY) + 1); level++) {
                int expPrev = this.levelExpMap.getOrDefault(level - 1, this.getLevelExpStart());
                String toCalc = expFormula.replace("%exp%", String.valueOf(expPrev));

                int expToLevel = level == this.getLevelStart() ? expPrev : (int) Evaluator.evaluate(toCalc);
                this.levelExpMap.put(level, expToLevel);
            }

            this.moneyMultiplier = new HashMap<>();
            for (JobState state : JobState.values()) {
                this.moneyMultiplier.put(state, new JobScaler(cfg, "Leveling.Money_Multiplier." + state.name(), this));
            }
            if (this.levelExpMap.isEmpty()) {
                throw new IllegalStateException("Empty level exp table!");
            }
        }

        this.objectives = new TreeMap<>();
        for (JYML configObjective : JYML.loadAll(cfg.getFile().getParentFile().getAbsolutePath() + "/objectives/", true)) {
            for (String objType : configObjective.getSection("")) {
                String path = objType + ".";

                boolean mEnabled = configObjective.getBoolean(path + "Enabled", true);
                if (!mEnabled) continue;

                double moChance = configObjective.getDouble(path + "Chance");
                double mMoneyMin = configObjective.getDouble(path + "Money.Min");
                double mMoneyMax = configObjective.getDouble(path + "Money.Max");

                int mExpMin = configObjective.getInt(path + "Exp.Min");
                int mExpMax = configObjective.getInt(path + "Exp.Max");

                IMoneyObjective objective = new MoneyObjective(objType, moChance, mMoneyMin, mMoneyMax, mExpMin, mExpMax);
                this.objectives.put(objective.getType(), objective);
            }
            configObjective.saveChanges();
        }
    }

    @Override
    public void onSave() {

    }

    @Override
    public void clear() {

    }

    @NotNull
    @Override
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(Placeholders.JOB_ID, this.getId())
            .replace(Placeholders.JOB_NAME, this.getName())
            .replace(Placeholders.JOB_DESCRIPTION, String.join("\n", this.getDescription()))
            .replace(Placeholders.JOB_ICON_NAME, ItemUtil.getItemName(this.getIcon()))
            .replace(Placeholders.JOB_ICON_LORE, String.join("\n", ItemUtil.getLore(this.getIcon())))
            ;
    }

    @Override
    public void onJobEvent(@NotNull E event, @NotNull Player player, @NotNull String object) {
        if (!this.hasPermission(player)) return;
        if (!MoneyManager.isMoneyAvailable(player)) return;

        IMoneyObjective moneyObjective = this.getObjective(object);
        if (moneyObjective == null) return;

        MoneyUser user = plugin.getUserManager().getUserData(player);
        UserJobData jobData = user.getJobData(this);
        //if (jobData.getState() == JobState.INACTIVE) return;
        if (!this.hasObjectiveUnlockLevelBypass(player) && moneyObjective.getUnlockLevel() > jobData.getJobLevel()) return;
        if (!Rnd.chance(moneyObjective.getChance())) return;

        ICurrency currency = this.getCurrency();
        double moneyRoll = moneyObjective.getMoney();
        double expRoll = moneyObjective.getExp();

        // Apply boosters and job modifiers only when objective returns positive amount of money
        // to prevent money multiply in negative way.
        if (moneyRoll > 0D) {
            if (jobData.isObjectWasted(moneyObjective, ObjectiveLimitType.MONEY) && !this.hasObjectiveLimitBypass(player, ObjectiveLimitType.MONEY)) {
                moneyRoll = 0D;
            }
            else {
                moneyRoll *= user.getBoosterMoney(this);
                moneyRoll *= jobData.getMoneyModifier();
            }
        }
        if (expRoll > 0D) {
            if (jobData.isObjectWasted(moneyObjective, ObjectiveLimitType.EXP) && !this.hasObjectiveLimitBypass(player, ObjectiveLimitType.EXP)) {
                expRoll = 0D;
            }
            else {
                expRoll *= user.getBoosterExp(this);
            }
        }

        // Give or take job exp for the objective no matter if there is a money item or not.
        user.addJobExp(this, moneyObjective.getType(), expRoll, false);

        if (moneyRoll == 0D || (moneyRoll < 1D && moneyRoll > 0D && currency.isIntegerOnly())) return;

        ItemStack moneyItem = currency.createMoney(moneyRoll, player, this, moneyObjective);
        if (moneyRoll <= 0D || currency.isDirectToBalance()) {
            this.plugin.getMoneyManager().pickupMoney(player, moneyItem);
            return;
        }

        this.handleEvent(event, player, moneyObjective, moneyItem);
    }

    protected abstract void handleEvent(@NotNull E event, @NotNull Player player, @NotNull IMoneyObjective objective, @NotNull ItemStack moneyItem);

    @Override
    public boolean hasPermission(@NotNull Player player) {
        return !this.isPermissionRequired() || player.hasPermission(Perms.JOB + this.getId());
    }

    @Override
    public boolean hasObjectiveUnlockLevelBypass(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean hasObjectiveLimitBypass(@NotNull Player player, ObjectiveLimitType limitType) {
        return true;
    }

    @Override
    @NotNull
    public JobType getType() {
        return this.type;
    }

    @NotNull
    @Override
    public ICurrency getCurrency() {
        return currency;
    }

    @Override
    @NotNull
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isPermissionRequired() {
        return this.isPermissionRequired;
    }

    @NotNull
    @Override
    public List<String> getDescription() {
        return description;
    }

    @NotNull
    @Override
    public ItemStack getIcon() {
        return new ItemStack(icon);
    }

    @NotNull
    @Override
    public JobState getStateDefault() {
        return stateDefault;
    }

    @NotNull
    @Override
    public Set<JobState> getStateAllowed() {
        return stateAllowed;
    }

    @Override
    public int getLevelExpStart() {
        return levelExpStart;
    }

    @Override
    public int getExpForLevel(int jobLevel) {
        Map.Entry<Integer, Integer> entry = this.levelExpMap.floorEntry(jobLevel);
        return entry != null ? entry.getValue() : this.getLevelExpStart();
    }

    @Override
    public int getLevelStart() {
        return levelStart;
    }

    @Override
    public int getLevelMax(@NotNull JobState state) {
        return levelMax.getOrDefault(state, 0);
    }

    @Override
    public double getMoneyMultiplier(@NotNull JobState state, int jobLevel) {
        Scaler scaler = this.moneyMultiplier.get(state);
        return scaler == null ? 1D : scaler.getValue(jobLevel);
    }

    @NotNull
    @Override
    public Collection<IMoneyObjective> getObjectives() {
        return objectives.values();
    }

    @Override
    @Nullable
    public IMoneyObjective getObjective(@NotNull String id) {
        return this.objectives.get(id.toLowerCase());
    }
}
