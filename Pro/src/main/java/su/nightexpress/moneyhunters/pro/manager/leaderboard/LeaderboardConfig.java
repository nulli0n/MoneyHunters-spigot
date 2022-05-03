package su.nightexpress.moneyhunters.pro.manager.leaderboard;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.moneyhunters.pro.Keys;
import su.nightexpress.moneyhunters.pro.MoneyHuntersAPI;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardConfig {

    public static JYML cfg;

    public static int                                genericUpdateMinutes;
    public static String                             genericMissingStatName;
    public static int                                maxBoardScores;
    public static Map<LeaderboardType, String[]>     signsFormat;
    public static Map<LeaderboardType, List<String>> hologramFormat;

    public static void load(@NotNull JYML cfg) {
        LeaderboardConfig.cfg = cfg;

        String path = "Generic.";
        genericUpdateMinutes = cfg.getInt(path + "Update_Interval_Minutes", 30) * 60;
        genericMissingStatName = cfg.getString(path + "Missing_Stat_Name", "MHF_Question");
        maxBoardScores = cfg.getInt(path + "Max_Board_Scores", 10);

        signsFormat = new HashMap<>();
        hologramFormat = new HashMap<>();
        for (LeaderboardType type : LeaderboardType.values()) {
            signsFormat.put(type, StringUtil.color(cfg.getStringList("Signs." + type.name() + ".Format")).toArray(new String[4]));
            hologramFormat.put(type, StringUtil.color(cfg.getStringList("Holograms." + type.name() + ".Format")));
        }
    }

    @NotNull
    public static List<String> getHologramFormat(@NotNull LeaderboardType type) {
        return LeaderboardConfig.hologramFormat.getOrDefault(type, Collections.emptyList());
    }

    @NotNull
    public static Collection<Sign> loadSigns(@NotNull LeaderboardType type) {
        return LocationUtil.deserialize(cfg.getStringList("Signs." + type.name() + ".Locations")).stream()
            .map(Location::getBlock)
            .map(block -> block.getState() instanceof Sign sign ? sign : null)
            .filter(sign -> sign != null && PDCUtil.getStringData(sign, Keys.LEADERBOARD_TYPE) != null)
            .collect(Collectors.toCollection(HashSet::new));
    }

    public static void saveSigns(@NotNull LeaderboardType type, @NotNull Set<Sign> signs) {
        cfg.set("Signs." + type.name() + ".Locations", LocationUtil.serialize(signs.stream().map(Sign::getLocation).toList()));
        cfg.saveChanges();
    }

    @NotNull
    public static Map<String, Collection<Location>> loadHolograms(@NotNull LeaderboardType type) {
        Map<String, Collection<Location>> map = new HashMap<>();

        String path = "Holograms." + type.name() + ".";
        cfg.getSection(path + "Locations").forEach(jobId -> {
            if (MoneyHuntersAPI.getJobManager().getJobById(jobId) == null) return;
            map.put(jobId, LocationUtil.deserialize(cfg.getStringList(path + "Locations." + jobId)));
        });

        return map;
    }

    public static void saveHolograms(@NotNull LeaderboardType type, @NotNull String jobId, @NotNull Collection<Location> locations) {
        cfg.set("Holograms." + type.name() + ".Locations." + jobId, LocationUtil.serialize(locations));
        cfg.saveChanges();
    }
}
