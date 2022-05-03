package su.nightexpress.moneyhunters.basic.manager.booster;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.api.booster.IBooster;
import su.nightexpress.moneyhunters.basic.api.job.IJob;
import su.nightexpress.moneyhunters.basic.config.Config;
import su.nightexpress.moneyhunters.basic.data.object.MoneyUser;
import su.nightexpress.moneyhunters.basic.manager.booster.listener.BoosterListenerGeneric;
import su.nightexpress.moneyhunters.basic.manager.booster.task.BoosterTask;

import java.util.*;

public class BoosterManager extends AbstractManager<MoneyHunters> {

    private Set<IBooster> boostersAuto;
    private Set<IBooster> boostersGlobal;

    private BoosterTask boosterTask;

    public BoosterManager(@NotNull MoneyHunters plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.boostersAuto = new HashSet<>();
        this.boostersGlobal = new HashSet<>();

        this.boosterTask = new BoosterTask(this.plugin);
        this.boosterTask.start();

        this.addListener(new BoosterListenerGeneric(this.plugin));
    }

    @Override
    protected void onShutdown() {
        if (this.boosterTask != null) {
            this.boosterTask.stop();
            this.boosterTask = null;
        }
        this.boostersGlobal.clear();
        this.boostersAuto.clear();
    }

    public static double getBoosterMoney(@NotNull Collection<IBooster> boosters) {
        return (boosters.stream().mapToDouble(IBooster::getMoneyPercent).sum() + 100D) / 100D;
    }

    public static double getBoosterExp(@NotNull Collection<IBooster> boosters) {
        return (boosters.stream().mapToDouble(IBooster::getExpPercent).sum() + 100D) / 100D;
    }

    public static double getBoosterMoneyPercent(@NotNull Collection<IBooster> boosters) {
        return boosters.stream().mapToDouble(IBooster::getMoneyPercent).sum();
    }

    public static double getBoosterExpPercent(@NotNull Collection<IBooster> boosters) {
        return boosters.stream().mapToDouble(IBooster::getExpPercent).sum();
    }

    @NotNull
    public Collection<IBooster> getBoostersAuto() {
        return this.boostersAuto;
    }

    @NotNull
    public Collection<IBooster> getBoostersAuto(@NotNull IJob<?> job) {
        return this.getBoostersAuto().stream().filter(booster -> booster.isApplicable(job)).toList();
    }

    @NotNull
    public Set<IBooster> getBoostersGlobal() {
        return boostersGlobal;
    }

    @NotNull
    public Collection<IBooster> getBoostersGlobal(@NotNull IJob<?> job) {
        return this.getBoostersGlobal().stream().filter(booster -> booster.isApplicable(job)).toList();
    }

    public void updateBoosters() {
        this.getBoostersGlobal().removeIf(IBooster::isExpired);
        this.getBoostersAuto().removeIf(IBooster::isExpired);
        this.getBoostersAuto().addAll(Config.getBoosters().stream().filter(IBooster::isActive).toList());

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!plugin.getUserManager().isLoaded(player)) continue;

            MoneyUser user = plugin.getUserManager().getOrLoadUser(player);
            user.updateBoosters();
        }
    }

    public void notifyBooster() {
        this.notifyBooster(plugin.getServer().getOnlinePlayers().toArray(new Player[0]));
    }

    public void notifyBooster(@NotNull Player... players) {
        List<IBooster> boosters = new ArrayList<>();
        boosters.addAll(this.getBoostersAuto());
        boosters.addAll(this.getBoostersGlobal());
        if (boosters.isEmpty()) return;

        List<String> message = plugin.lang().Booster_Global_Notify.asList();

        for (Player player : players) {
            for (String line : message) {
                if (line.contains("%booster_")) {
                    boosters.forEach(booster -> {
                        MessageUtil.sendWithJSON(player, booster.replacePlaceholders().apply(line));
                    });
                }
                else MessageUtil.sendWithJSON(player, line);
            }
        }
    }
}
