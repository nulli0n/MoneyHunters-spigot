package su.nightexpress.moneyhunters.pro.api.money;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.MythicMobsHook;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.currency.ICurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public abstract class AbstractMoneyObjective implements IMoneyObjective {

    protected String type;
    protected double chance;
    protected double moneyMin;
    protected double moneyMax;
    protected int    expMin;
    protected int    expMax;

    protected int                             unlockLevel;
    protected Map<ObjectiveLimitType, Scaler> limit;

    public AbstractMoneyObjective(
        @NotNull String type,
        double chance,
        double moneyMin,
        double moneyMax,
        int expMin,
        int expMax,

        int unlockLevel,
        @NotNull Map<ObjectiveLimitType, Scaler> limit
    ) {
        this.setType(type);
        this.setChance(chance);
        this.setMoneyMin(moneyMin);
        this.setMoneyMax(moneyMax);
        this.setExpMin(expMin);
        this.setExpMax(expMax);
        this.setUnlockLevel(unlockLevel);

        this.limit = new HashMap<>();
        this.limit.putAll(limit);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(Placeholders.OBJECTIVE_NAME, this.getName())
            .replace(Placeholders.OBJECTIVE_CHANCE, NumberUtil.format(this.getChance()))
            .replace(Placeholders.OBJECTIVE_EXP_MIN, NumberUtil.format(this.getExpMin()))
            .replace(Placeholders.OBJECTIVE_EXP_MAX, NumberUtil.format(this.getExpMax()))
            .replace(Placeholders.OBJECTIVE_UNLOCK_LEVEL, String.valueOf(this.getUnlockLevel()))
            ;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(@NotNull ICurrency currency, int jobLevel) {
        return str -> this.replacePlaceholders().apply(str
            .replace(Placeholders.OBJECTIVE_MONEY_MIN, currency.format(this.getMoneyMin()))
            .replace(Placeholders.OBJECTIVE_MONEY_MAX, currency.format(this.getMoneyMax()))
            .replace(Placeholders.OBJECTIVE_LIMIT_MONEY, currency.format(this.getDailyLimit(ObjectiveLimitType.MONEY, jobLevel)))
            .replace(Placeholders.OBJECTIVE_LIMIT_EXP, NumberUtil.format(this.getDailyLimit(ObjectiveLimitType.EXP, jobLevel)))
        );
    }

    @Override
    @NotNull
    public String getType() {
        return this.type;
    }

    @Override
    @NotNull
    public String getName() {
        String typeRaw = this.getType().toUpperCase();

        Material material = Material.getMaterial(typeRaw);
        if (material != null) {
            return NexEngine.get().getLangManager().getEnum(material);
        }

        if (Hooks.hasMythicMobs() && MythicMobsHook.getMobConfig(typeRaw) != null) {
            return MythicMobsHook.getMobDisplayName(typeRaw);
        }

        EntityType entityType = CollectionsUtil.getEnum(typeRaw, EntityType.class);
        if (entityType != null) {
            return NexEngine.get().getLangManager().getEnum(entityType);
        }

        return this.getType();
    }

    @Override
    public void setType(@NotNull String type) {
        this.type = type.toLowerCase();
    }

    @Override
    public double getChance() {
        return this.chance;
    }

    @Override
    public void setChance(double chance) {
        this.chance = chance;
    }

    @Override
    public double getMoneyMin() {
        return moneyMin;
    }

    @Override
    public void setMoneyMin(double moneyMin) {
        this.moneyMin = moneyMin;
    }

    @Override
    public double getMoneyMax() {
        return moneyMax;
    }

    @Override
    public void setMoneyMax(double moneyMax) {
        this.moneyMax = moneyMax;
    }

    @Override
    public int getExpMin() {
        return expMin;
    }

    @Override
    public void setExpMin(int expMin) {
        this.expMin = expMin;
    }

    @Override
    public int getExpMax() {
        return expMax;
    }

    @Override
    public void setExpMax(int expMax) {
        this.expMax = expMax;
    }

    @Override
    public int getUnlockLevel() {
        return unlockLevel;
    }

    @Override
    public void setUnlockLevel(int unlockLevel) {
        this.unlockLevel = unlockLevel;
    }

    @Override
    public double getDailyLimit(@NotNull ObjectiveLimitType type, int level) {
        Scaler scaler = this.limit.get(type);
        return scaler == null ? 0D : scaler.getValue(level);
    }

    @Override
    public void setDailyLimit(@NotNull ObjectiveLimitType type, @NotNull Scaler scaler) {
        this.limit.put(type, scaler);
    }
}
