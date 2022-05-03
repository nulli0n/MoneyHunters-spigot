package su.nightexpress.moneyhunters.pro.api.job;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.currency.ICurrency;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.pro.api.money.ObjectiveLimitType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IJob<E extends Event> extends ICleanable, IPlaceholder {

    void onJobEvent(@NotNull E event, @NotNull Player player, @NotNull String object);

    @NotNull MoneyHunters plugin();

    @NotNull String getId();

    @NotNull JobType getType();

    @NotNull ICurrency getCurrency();

    @NotNull String getName();

    @NotNull ItemStack getIcon();

    @NotNull List<String> getDescription();

    boolean isPermissionRequired();

    boolean hasPermission(@NotNull Player player);

    boolean hasObjectiveUnlockLevelBypass(@NotNull Player player);

    boolean hasObjectiveLimitBypass(@NotNull Player player, ObjectiveLimitType limitType);

    @NotNull JobState getStateDefault();

    @NotNull Set<JobState> getStateAllowed();

    default boolean isStateAllowed(@NotNull JobState state) {
        return this.getStateAllowed().contains(state);
    }

    default boolean isStateAllowedAny() {
        return !this.getStateAllowed().isEmpty();
    }

    int getLevelExpStart();

    int getExpForLevel(int level);

    double getMoneyMultiplier(@NotNull JobState state, int jobLevel);

    int getLevelStart();

    int getLevelMax(@NotNull JobState state);

    @NotNull AbstractMenu<?> getObjectivesMenu();

    @NotNull Collection<IMoneyObjective> getObjectives();

    @Nullable IMoneyObjective getObjective(@NotNull String id);

    default boolean hasObjective(@NotNull String id) {
        return this.getObjective(id) != null;
    }
}
