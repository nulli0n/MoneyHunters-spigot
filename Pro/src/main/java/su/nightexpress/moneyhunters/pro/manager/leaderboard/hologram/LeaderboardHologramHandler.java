package su.nightexpress.moneyhunters.pro.manager.leaderboard.hologram;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.ILoadable;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardType;

public interface LeaderboardHologramHandler extends ILoadable {

    void createHologram(@NotNull LeaderboardType type, @NotNull String jobId, @NotNull Location location);

    void removeHologram(@NotNull Location location);

    void updateHolograms();

    void updateHologram(@NotNull LeaderboardType type, @NotNull String jobId);
}
