package su.nightexpress.moneyhunters.pro.api.currency;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.*;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.moneyhunters.pro.Keys;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.pro.config.Config;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.UnaryOperator;

public abstract class AbstractCurrency implements ICurrency {

    protected String       id;
    protected String       name;
    protected String       formatDisplay;
    protected NumberFormat formatAmount;
    protected boolean      isDirectToBalance;
    protected boolean      isIntegerOnly;

    protected Particle dropEffectParticle;
    protected String   dropEffectParticleData;
    protected Sound    pickupEffectSound;

    protected boolean  isDeathPenaltyEnabled;
    protected boolean  isDeathPenaltyDropItem;
    protected double   deathPenaltyChance;
    protected double[] deathPenaltyAmount;

    protected TreeMap<Integer, ItemStack> itemStyle;

    public AbstractCurrency(@NotNull MoneyHunters plugin, @NotNull JYML cfg) {
        // --------------- UPDATE --------------- //
        String oldParticle = cfg.getString("Visual_Effects.Particle_Drop", "");
        String oldSound = cfg.getString("Visual_Effects.Sound_Pickup", "");
        cfg.addMissing("Visual_Effects.Drop.Particle.Name", oldParticle);
        cfg.addMissing("Visual_Effects.Drop.Particle.Data", "");
        cfg.addMissing("Visual_Effects.Pickup.Sound", oldSound);
        cfg.remove("Visual_Effects.Particle_Drop");
        cfg.remove("Visual_Effects.Sound_Pickup");
        cfg.saveChanges();
        // ------------- END UPDATE ------------- //

        this.id = cfg.getFile().getName().replace(".yml", "");
        this.name = Colorizer.apply(cfg.getString("Name", id));
        this.formatDisplay = Colorizer.apply(cfg.getString("Format_Display", Placeholders.CURRENCY_NAME + Placeholders.GENERIC_AMOUNT));
        this.formatAmount = new DecimalFormat(cfg.getString("Format_Amount", "###,###.##"), new DecimalFormatSymbols(Locale.ENGLISH));
        this.isDirectToBalance = cfg.getBoolean("Direct_To_Balance");
        this.isIntegerOnly = cfg.getBoolean("Integer_Only");

        this.dropEffectParticle = cfg.getEnum("Visual_Effects.Drop.Particle.Name", Particle.class);
        this.dropEffectParticleData = cfg.getString("Visual_Effects.Drop.Particle.Data", "");
        this.pickupEffectSound = cfg.getEnum("Visual_Effects.Pickup.Sound", Sound.class);

        String path = "Death_Penalty.";
        this.isDeathPenaltyEnabled = cfg.getBoolean(path + "Enabled");
        this.isDeathPenaltyDropItem = !cfg.getBoolean(path + "Do_Not_Drop_Item");
        this.deathPenaltyChance = cfg.getDouble(path + "Chance");
        this.deathPenaltyAmount = new double[2];
        this.deathPenaltyAmount[0] = cfg.getDouble(path + "Percent_Of_Balance.Minimal");
        this.deathPenaltyAmount[1] = cfg.getDouble(path + "Percent_Of_Balance.Maximal");

        this.itemStyle = new TreeMap<>();
        for (String sVal : cfg.getSection("Item_Style_By_Amount")) {
            int amount = StringUtil.getInteger(sVal, 0);
            path = "Item_Style_By_Amount." + sVal + ".";

            ItemStack moneyItem = cfg.getItem(path);
            if (moneyItem.getType().isAir()) {
                plugin.error(id + "Currency: Invalid item for '" + sVal + "' money amount!");
                continue;
            }

            itemStyle.put(amount, moneyItem);
        }
        if (this.itemStyle.isEmpty()) {
            this.isDirectToBalance = true;
            plugin.warn(id + "Currency: No money items are defined, money will be given directly to player balance.");
        }
    }

    public AbstractCurrency(
        @NotNull String id, @NotNull String name,
        @NotNull String formatDisplay, @NotNull NumberFormat formatAmount,
        boolean isDirectToBalance, boolean isIntegerOnly,

        @NotNull Particle dropEffectParticle, @NotNull String dropEffectParticleData,
        @Nullable Sound pickupEffectSound,

        boolean isDeathPenaltyEnabled, boolean isDeathPenaltyDropItem,
        double deathPenaltyChance, double[] deathPenaltyAmount,

        @NotNull TreeMap<Integer, ItemStack> itemStyle) {
        this.id = id.toLowerCase();
        this.name = Colorizer.apply(name);
        this.formatDisplay = Colorizer.apply(formatDisplay);
        this.formatAmount = formatAmount;
        this.isDirectToBalance = isDirectToBalance;
        this.isIntegerOnly = isIntegerOnly;

        this.dropEffectParticle = dropEffectParticle;
        this.dropEffectParticleData = dropEffectParticleData;
        this.pickupEffectSound = pickupEffectSound;

        this.isDeathPenaltyEnabled = isDeathPenaltyEnabled;
        this.isDeathPenaltyDropItem = isDeathPenaltyDropItem;
        this.deathPenaltyChance = deathPenaltyChance;
        this.deathPenaltyAmount = deathPenaltyAmount;

        this.itemStyle = itemStyle;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(Placeholders.CURRENCY_NAME, this.getName()
            );
    }

    @Override
    @NotNull
    public final String getId() {
        return this.id;
    }

    @Override
    @NotNull
    public final String getName() {
        return this.name;
    }

    @NotNull
    @Override
    public String getFormatDisplay() {
        return formatDisplay;
    }

    @NotNull
    @Override
    public NumberFormat getFormatAmount() {
        return formatAmount;
    }

    @Override
    public boolean isDirectToBalance() {
        return isDirectToBalance;
    }

    @Override
    public boolean isIntegerOnly() {
        return isIntegerOnly;
    }

    @Nullable
    @Override
    public Particle getDropEffectParticle() {
        return dropEffectParticle;
    }

    @NotNull
    @Override
    public String getDropEffectParticleData() {
        return dropEffectParticleData;
    }

    @Nullable
    @Override
    public Sound getPickupEffectSound() {
        return pickupEffectSound;
    }

    @Override
    public boolean isDeathPenaltyEnabled() {
        return isDeathPenaltyEnabled;
    }

    @Override
    public boolean isDeathPenaltyDropItem() {
        return isDeathPenaltyDropItem;
    }

    @Override
    public double getDeathPenaltyChance() {
        return deathPenaltyChance;
    }

    @Override
    public double getDeathPenaltyAmountMin() {
        return this.deathPenaltyAmount[0];
    }

    @Override
    public double getDeathPenaltyAmountMax() {
        return this.deathPenaltyAmount[1];
    }

    @Override
    @NotNull
    public String format(double amount) {
        String sAmount = this.getFormatAmount().format(this.round(amount));
        String sDisplay = this.getFormatDisplay().replace(Placeholders.GENERIC_AMOUNT, sAmount);
        return this.replacePlaceholders().apply(sDisplay);
    }

    @Override
    public double round(double amount) {
        return this.isIntegerOnly() ? (int) amount : NumberUtil.round(amount);
    }

    public double getDeathPenaltyAmount() {
        return Rnd.getDouble(this.getDeathPenaltyAmountMin(), this.getDeathPenaltyAmountMax());
    }

    public void playDropParticle(@NotNull Location location) {
        if (this.getDropEffectParticle() == null) return;
        EffectUtil.playEffect(location, this.getDropEffectParticle(), this.getDropEffectParticleData(), 0.35, 0.5, 0.35, 0.2f, 20);
    }

    @Override
    @NotNull
    public ItemStack getMoneyItem(double amount) {
        Map.Entry<Integer, ItemStack> e = this.itemStyle.floorEntry((int) Math.abs(amount));
        if (e == null) {
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(e.getValue());
    }

    @Override
    @NotNull
    public ItemStack createMoney(double money, @Nullable Player owner, @Nullable IJob<?> job, @Nullable IMoneyObjective objective) {
        money = round(money);
        if (money == 0D) throw new IllegalArgumentException("Money amount can not be zero!");

        // Get item model depends on the money amount.
        ItemStack item = this.getMoneyItem(money);

        // Set money and job (if present) tags.
        double finalMoney = money;
        ItemUtil.mapMeta(item, meta -> {
            PDCUtil.set(meta, Keys.MONEY_AMOUNT, finalMoney);
            PDCUtil.set(meta, Keys.MONEY_CURRENCY, this.getId());
            PDCUtil.set(meta, Keys.MONEY_ID, UUID.randomUUID().toString());
            if (job != null) PDCUtil.set(meta, Keys.MONEY_JOB, job.getId());
            if (objective != null) PDCUtil.set(meta, Keys.MONEY_OBJECTIVE, objective.getType());

            // Add owner protection for money item.
            if (Config.MONEY_OWNER_PROTECTION_ENABLED && owner != null) {
                PDCUtil.set(meta, Keys.MONEY_OWNER, owner.getName());
            }

            // And now replace visuals.
            if (meta.hasDisplayName()) {
                String name = Colorizer.apply(meta.getDisplayName().replace(Placeholders.GENERIC_MONEY, this.format(finalMoney)));
                meta.setDisplayName(name);
            }
        });
        return item;
    }
}
