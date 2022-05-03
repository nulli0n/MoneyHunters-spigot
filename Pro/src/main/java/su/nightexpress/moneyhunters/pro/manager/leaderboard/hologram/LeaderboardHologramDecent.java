package su.nightexpress.moneyhunters.pro.manager.leaderboard.hologram;


import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardConfig;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardManager;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardType;

import java.util.*;

public class LeaderboardHologramDecent implements LeaderboardHologramHandler {

    private final LeaderboardManager leaderboardManager;

    private Map<LeaderboardType, Map<String, Set<Hologram>>> holograms;

    public LeaderboardHologramDecent(@NotNull LeaderboardManager leaderboardManager) {
        this.leaderboardManager = leaderboardManager;
    }

    @Override
    public void setup() {
        this.holograms = new HashMap<>();

        for (LeaderboardType type : LeaderboardType.values()) {
            LeaderboardConfig.loadHolograms(type).forEach((jobId, locations) -> {
                locations.forEach(loc -> createHologram(type, jobId, loc));
            });
        }
    }

    @Override
    public void shutdown() {
        this.holograms.forEach((type, map) -> {
            map.forEach((jobId, holograms) -> {
                LeaderboardConfig.saveHolograms(type, jobId, holograms.stream().map(Hologram::getLocation).toList());
                holograms.forEach(Hologram::delete);
            });
        });
        this.holograms.clear();
    }

    @Override
    public void createHologram(@NotNull LeaderboardType type, @NotNull String jobId, @NotNull Location location) {
        Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), location);
        this.getHolograms(type, jobId).add(hologram);
        this.updateHologram(type, jobId);
    }

    @Override
    public void removeHologram(@NotNull Location loc) {
        this.holograms.values().forEach(map -> map.values().forEach(set -> {
            Optional<Hologram> opt = set.stream().filter(h -> h.getLocation().distance(loc) <= 7.5).findFirst();
            if (opt.isPresent()) {
                set.remove(opt.get());
                opt.get().delete();
            }
        }));
    }

    @NotNull
    private Map<String, Set<Hologram>> getHolograms(@NotNull LeaderboardType type) {
        return this.holograms.computeIfAbsent(type, map -> new HashMap<>());
    }

    @NotNull
    private Set<Hologram> getHolograms(@NotNull LeaderboardType type, @NotNull String jobId) {
        return this.getHolograms(type).computeIfAbsent(jobId, set -> new HashSet<>());
    }

    @Override
    public void updateHolograms() {
        for (LeaderboardType boardType : LeaderboardType.values()) {
            this.getHolograms(boardType).keySet().forEach(jobId -> {
                this.updateHologram(boardType, jobId);
            });
        }
    }

    @Override
    public void updateHologram(@NotNull LeaderboardType type, @NotNull String jobId) {
        IJob<?> job = this.leaderboardManager.plugin().getJobManager().getJobById(jobId);
        if (job == null) return;

        List<String> format = this.leaderboardManager.formatLeaderList(LeaderboardConfig.getHologramFormat(type), type, job);
        this.getHolograms(type, jobId).forEach(holo -> {
            HologramPage page = holo.getPage(0);
            while (page.size() > 0) page.removeLine(0);

            format.forEach(text -> {
                page.addLine(new HologramLine(page, page.getNextLineLocation(), text));
            });
        });
    }
}
