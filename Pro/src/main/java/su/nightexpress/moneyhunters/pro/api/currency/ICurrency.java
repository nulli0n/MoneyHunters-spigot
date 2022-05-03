package su.nightexpress.moneyhunters.pro.api.currency;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;

import java.text.NumberFormat;

public interface ICurrency extends IPlaceholder {

    @NotNull String getId();

    @NotNull String getName();

    @NotNull String getFormatDisplay();

    @NotNull NumberFormat getFormatAmount();

    @NotNull String format(double amount);

    double round(double amount);

    boolean isDirectToBalance();

    boolean isIntegerOnly();

    @Nullable Particle getDropEffectParticle();

    @NotNull String getDropEffectParticleData();

    @Nullable Sound getPickupEffectSound();

    boolean isDeathPenaltyEnabled();

    boolean isDeathPenaltyDropItem();

    double getDeathPenaltyChance();

    double getDeathPenaltyAmountMin();

    double getDeathPenaltyAmountMax();

    double getDeathPenaltyAmount();

    void playDropParticle(@NotNull Location location);

    @NotNull
    ItemStack createMoney(double money, @Nullable Player owner, @Nullable IJob<?> job, @Nullable IMoneyObjective objective);

    @NotNull ItemStack getMoneyItem(double amount);

    double getBalance(@NotNull Player player);

    void give(@NotNull Player player, double amount);

    void take(@NotNull Player player, double amount);
}
