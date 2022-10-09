package su.nightexpress.moneyhunters.pro;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.moneyhunters.pro.api.currency.ICurrency;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.manager.job.JobManager;
import su.nightexpress.moneyhunters.pro.manager.money.MoneyManager;

import java.util.Collection;

public class MoneyHuntersAPI {

    public static final MoneyHunters PLUGIN = MoneyHunters.getPlugin(MoneyHunters.class);

    @NotNull
    public static MoneyUser getPlayerData(@NotNull Player player) {
        return PLUGIN.getUserManager().getUserData(player);
    }

    @Nullable
    public static ICurrency getCurrency(@NotNull String id) {
        return PLUGIN.getCurrencyManager().getCurrency(id);
    }

    @NotNull
    public static MoneyManager getMoneyManager() {
        return PLUGIN.getMoneyManager();
    }

    @NotNull
    public static JobManager getJobManager() {
        return PLUGIN.getJobManager();
    }

    @Nullable
    public static IJob<?> getJobById(@NotNull String id) {
        return PLUGIN.getJobManager().getJobById(id);
    }

    @NotNull
    public static Collection<IJob<?>> getJobs() {
        return PLUGIN.getJobManager().getJobs();
    }
}
