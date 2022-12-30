package su.nightexpress.moneyhunters.pro.api.job;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractLoadableItem;
import su.nexmedia.engine.utils.*;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.currency.ICurrency;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.pro.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.pro.config.Config;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;
import su.nightexpress.moneyhunters.pro.manager.currency.CurrencyId;
import su.nightexpress.moneyhunters.pro.manager.job.menu.JobObjectivesMenu;
import su.nightexpress.moneyhunters.pro.manager.money.MoneyManager;
import su.nightexpress.moneyhunters.pro.manager.money.object.MoneyObjective;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    protected JobObjectivesMenu            objectivesMenu;

    public AbstractJob(@NotNull MoneyHunters plugin, @NotNull JYML cfg, @NotNull JobType jobType) {
        super(plugin, cfg);

        // ------------------- UPDATE CONFIGURATION ------------------- //
        cfg.addMissing("State.Default", JobState.PRIMARY.name());
        cfg.addMissing("State.Allowed", Stream.of(JobState.values()).map(Enum::name).toList());

        if (!cfg.isConfigurationSection("Leveling.Money_Multiplier")) {
            int oldLevelMax = cfg.getInt("Leveling.Max_Level", -1);
            String oldMoneyMultiplier = cfg.getString("Leveling.Money_Multiplier", "1");

            cfg.remove("Leveling.Max_Level");
            cfg.remove("Leveling.Money_Multiplier");

            for (JobState state : JobState.values()) {
                cfg.addMissing("Leveling.Max_Level." + state.name(), oldLevelMax);
                cfg.addMissing("Leveling.Money_Multiplier." + state.name(), oldMoneyMultiplier);
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

        this.stateDefault = cfg.getEnum("State.Default", JobState.class, JobState.PRIMARY);
        this.stateAllowed = cfg.getStringSet("State.Allowed").stream()
            .map(raw -> CollectionsUtil.getEnum(raw, JobState.class)).collect(Collectors.toCollection(HashSet::new));

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

                int mUnlockLevel = configObjective.getInt(path + "Unlock.Job_Min_Level");

                Map<ObjectiveLimitType, Scaler> limit = new HashMap<>();
                for (ObjectiveLimitType limitType : ObjectiveLimitType.values()) {
                    limit.put(limitType, new JobScaler(configObjective, path + "Daily_Limits." + limitType.name(), this));
                }

                IMoneyObjective objective = new MoneyObjective(objType, moChance, mMoneyMin, mMoneyMax, mExpMin, mExpMax, mUnlockLevel, limit);
                this.objectives.put(objective.getType(), objective);
            }
            configObjective.saveChanges();
        }
        this.objectivesMenu = new JobObjectivesMenu(plugin, this);
    }

    /*private void lazyCreate(@NotNull JYML cfg) {
        Set<String> objectives = new HashSet<>(cfg.getSection(""));
        //Set<Material> materials = Stream.of(Material.values()).collect(Collectors.toSet());
        if (this.getId().equalsIgnoreCase("hunting")) {
            objectives.addAll(Stream.of(EntityType.values()).filter(e -> e.isSpawnable() && e.isAlive()).map(Enum::name).collect(Collectors.toSet()));
        }
        if (this.getId().equalsIgnoreCase("mining")) {
            objectives.addAll(materials.stream().map(Enum::name).filter(s -> s.contains("_ORE")).collect(Collectors.toSet()));
            objectives.add(Material.STONE.name()); objectives.add(Material.ANDESITE.name());
            objectives.add(Material.DIORITE.name()); objectives.add(Material.GRANITE.name());
            objectives.add(Material.COBBLESTONE.name()); objectives.add(Material.MOSSY_COBBLESTONE.name());
            objectives.add(Material.NETHERRACK.name());
        }
        else if (this.getId().equalsIgnoreCase("digging")) {
            objectives.add(Material.GRASS_BLOCK.name()); objectives.add(Material.DIRT.name());
            objectives.add(Material.COARSE_DIRT.name()); objectives.add(Material.SAND.name());
            objectives.add(Material.RED_SAND.name()); objectives.add(Material.GRAVEL.name());
            objectives.add(Material.SOUL_SAND.name()); objectives.add(Material.CLAY.name());
            objectives.add(Material.SNOW_BLOCK.name()); objectives.add(Material.MYCELIUM.name());
            objectives.add(Material.PODZOL.name());
        }
        else if (this.getId().equalsIgnoreCase("hunting")) {

        }
        else if (this.getId().equalsIgnoreCase("farming")) {
            objectives.addAll(materials.stream().filter(s -> s.isBlock() && s.createBlockData() instanceof Ageable).map(Enum::name).collect(Collectors.toSet()));
        }
        else if (this.getId().equalsIgnoreCase("fishing")) {

        }

        objectives.forEach(cfg::remove);

        objectives.stream().sorted(String::compareTo).forEach(objective -> {
            String path = objective.toUpperCase() + ".";
            cfg.set(path + "Enabled", true);
            cfg.set(path + "Chance", 30D);
            cfg.set(path + "Money.Min", 0.5D);
            cfg.set(path + "Money.Max", 5D);
            cfg.set(path + "Exp.Min", 1);
            cfg.set(path + "Exp.Max", 3);
            cfg.set(path + "Unlock.Job_Min_Level", 0);
            cfg.set(path + "Daily_Limits.MONEY", "-1");
            cfg.set(path + "Daily_Limits.EXP", "-1");
        });
    }*/

    @Override
    public void onSave() {

    }

    @Override
    public void clear() {
        if (this.objectivesMenu != null) {
            this.objectivesMenu.clear();
            this.objectivesMenu = null;
        }
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
        if (jobData.getState() == JobState.INACTIVE) return;
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
        return player.hasPermission(Perms.BYPASS_JOB_OBJECTIVE_LEVEL);
    }

    @Override
    public boolean hasObjectiveLimitBypass(@NotNull Player player, ObjectiveLimitType limitType) {
        return player.hasPermission(switch (limitType) {
            case EXP -> Perms.BYPASS_JOB_OBJECTIVE_LIMIT_EXP;
            case MONEY -> Perms.BYPASS_JOB_OBJECTIVE_LIMIT_MONEY;
        });
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

    @NotNull
    @Override
    public JobObjectivesMenu getObjectivesMenu() {
        return objectivesMenu;
    }
}
