package su.nightexpress.moneyhunters.pro.manager.leaderboard;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.moneyhunters.pro.Keys;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.config.Config;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;
import su.nightexpress.moneyhunters.pro.hooks.HookId;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.command.LeaderboardCommand;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.hologram.LeaderboardHologramDecent;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.hologram.LeaderboardHologramDisplays;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.hologram.LeaderboardHologramHandler;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.listener.LeaderboardListener;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.task.LeaderboardUpdateTask;

import java.util.*;
import java.util.stream.Stream;

public class LeaderboardManager extends AbstractManager<MoneyHunters> {

    private Map<LeaderboardType, Set<Sign>>                        signs;
    private Map<LeaderboardType, Map<String, Map<String, Double>>> stats;

    private LeaderboardUpdateTask      updateTask;
    private LeaderboardHologramHandler hologramHandler;

    public static final String PLACEHOLDER_NAME     = "%name%";
    public static final String PLACEHOLDER_POSITION = "%position%";
    public static final String PLACEHOLDER_TYPE     = "%type%";
    public static final String PLACEHOLDER_JOB      = "%job%";
    public static final String PLACEHOLDER_SCORE    = "%score%";

    public LeaderboardManager(@NotNull MoneyHunters plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        this.stats = new HashMap<>();
        this.signs = new HashMap<>();

        LeaderboardConfig.load(JYML.loadOrExtract(plugin, "leaderboards.yml"));
        this.plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Stream.of(LeaderboardType.values()).forEach(type -> this.getSigns(type).addAll(LeaderboardConfig.loadSigns(type)));
            this.updateSigns();
        }, 20L);

        if (Hooks.hasPlugin(HookId.HOLOGRAPHIC_DISPLAYS)) {
            this.hologramHandler = new LeaderboardHologramDisplays(this);
            this.hologramHandler.setup();
        }
        else if (Hooks.hasPlugin(HookId.DECENT_HOLOGRAMS)) {
            this.hologramHandler = new LeaderboardHologramDecent(this);
            this.hologramHandler.setup();
        }
        else {
            this.plugin().info("No compatible holograms plugin found. Leaderboard Holograms will be disabled.");
        }

        this.plugin.getMainCommand().addChildren(new LeaderboardCommand(this));

        if (LeaderboardConfig.genericUpdateMinutes > 0) {
            this.updateTask = new LeaderboardUpdateTask(this);
            this.updateTask.start();
        }

        this.addListener(new LeaderboardListener(this));
    }

    @Override
    public void onShutdown() {
        LeaderboardConfig.cfg.reload();

        if (this.updateTask != null) {
            this.updateTask.stop();
            this.updateTask = null;
        }
        if (this.hologramHandler != null) {
            this.hologramHandler.shutdown();
            this.hologramHandler = null;
        }

        for (LeaderboardType type : LeaderboardType.values()) {
            LeaderboardConfig.saveSigns(type, this.getSigns(type));
        }

        if (this.signs != null) {
            this.signs.clear();
            this.signs = null;
        }
    }

    @Nullable
    public LeaderboardHologramHandler getHologramHandler() {
        return this.hologramHandler;
    }

    @NotNull
    public List<String> formatLeaderList(@NotNull List<String> orig, @NotNull LeaderboardType type, @NotNull IJob<?> job) {
        List<String> list = new ArrayList<>();
        List<LeaderboardScore> scores = this.getScores(type, job.getId());

        orig.forEach(line -> {
            line = line.replace(PLACEHOLDER_JOB, job.getName()).replace(PLACEHOLDER_TYPE, plugin.getLangManager().getEnum(type));

            if (line.contains(PLACEHOLDER_NAME)) {
                for (int pos = 0; pos < LeaderboardConfig.maxBoardScores; pos++) {
                    if (pos >= scores.size()) break;

                    LeaderboardScore score = scores.get(pos);
                    String line2 = line
                        .replace(PLACEHOLDER_POSITION, String.valueOf(pos + 1))
                        .replace(PLACEHOLDER_NAME, score.name())
                        .replace(PLACEHOLDER_SCORE, NumberUtil.format(score.score()));
                    list.add(line2);
                }
                return;
            }
            list.add(line);
        });

        return list;
    }

    @NotNull
    public LeaderboardScore getScore(@NotNull LeaderboardType type, @NotNull IJob<?> job, int pos) {
        pos = Math.max(0, pos - 1);

        List<LeaderboardScore> scores = this.getScores(type, job.getId());
        return scores.size() > pos ? scores.get(pos) : LeaderboardScore.EMPTY;
    }

    @NotNull
    public Map<String, List<LeaderboardScore>> getScores(@NotNull LeaderboardType type) {
        Map<String, List<LeaderboardScore>> map = new HashMap<>();
        this.getScoresRaw(type).forEach((jobId, mapScore) -> {
            List<LeaderboardScore> scores = new ArrayList<>();
            mapScore.forEach((name, score) -> scores.add(new LeaderboardScore(name, score)));
            map.put(jobId, scores);
        });
        return map;
    }

    @NotNull
    public List<LeaderboardScore> getScores(@NotNull LeaderboardType type, @NotNull String jobId) {
        return this.getScores(type).getOrDefault(jobId.toLowerCase(), Collections.emptyList());
    }

    @NotNull
    public Map<String, Map<String, Double>> getScoresRaw(@NotNull LeaderboardType type) {
        return this.stats.computeIfAbsent(type, k -> new HashMap<>());
    }

    @NotNull
    public Map<String, Double> getScoresRaw(@NotNull LeaderboardType type, @NotNull String jobId) {
        return this.getScoresRaw(type).computeIfAbsent(jobId.toLowerCase(), map -> new HashMap<>());
    }

    @NotNull
    public Set<Sign> getSigns(@NotNull LeaderboardType type) {
        this.signs.computeIfAbsent(type, set -> new HashSet<>()).removeIf(sign -> !(sign.getBlock().getState() instanceof Sign));
        return this.signs.get(type);
    }

    public void updateStats() {
        if (Config.LEVELING_ENABLED) {
            this.getScores(LeaderboardType.TOP_LEVEL).clear();

            List<MoneyUser> users = plugin.getData().getUsers();
            plugin.getJobManager().getJobs().forEach(job -> {
                Map<String, Double> levels = this.getScoresRaw(LeaderboardType.TOP_LEVEL, job.getId());
                users.forEach(user -> {
                    UserJobData jobData = user.getJobData(job);
                    levels.put(user.getName(), (double) jobData.getJobLevel());
                });
            });
        }

        for (LeaderboardType boardType : LeaderboardType.values()) {
            if (boardType != LeaderboardType.TOP_LEVEL && boardType.nextDay()) {
                this.getScores(boardType).clear();
                boardType.setLastDay();
            }

			/*MoneyHuntersAPI.getJobManager().getJobIds().forEach(jobId -> {
				Map<String, Double> scores = this.getScoresRaw(boardType, jobId);
				scores.put("Nikkie", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
				scores.put("flower45", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
				scores.put("apple_lord", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
				scores.put("Nastia", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
				scores.put("Corvus", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
				scores.put("TriganD", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
				scores.put("firestorm", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
				scores.put("lPariahl", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
				scores.put("Dr_Romeo", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
				scores.put("RedCat", (double) Rnd.get(boardType == LeaderboardType.TOP_LEVEL ? 100 : 5000));
			});*/

            this.getScoresRaw(boardType).forEach((jobId, scores) -> {
                this.getScoresRaw(boardType).put(jobId, CollectionsUtil.sortDescent(scores));
            });
        }

        this.plugin.info("Leaderboards updated!");
    }

    public void updateSigns() {
        for (LeaderboardType boardType : LeaderboardType.values()) {
            this.getSigns(boardType).forEach(this::updateSign);
        }
    }

    public void addSign(@NotNull Sign sign, @NotNull LeaderboardType type, @NotNull IJob<?> job, int position) {
        PDCUtil.setData(sign, Keys.LEADERBOARD_JOB_ID, job.getId());
        PDCUtil.setData(sign, Keys.LEADERBOARD_POSITION, position);
        PDCUtil.setData(sign, Keys.LEADERBOARD_TYPE, type.name());
        this.getSigns(type).add(sign);
    }

    @SuppressWarnings("deprecation")
    public void updateSign(@NotNull Sign sign) {
        int pos = PDCUtil.getIntData(sign, Keys.LEADERBOARD_POSITION);
        String jobId = PDCUtil.getStringData(sign, Keys.LEADERBOARD_JOB_ID);
        if (jobId == null) return;

        IJob<?> job = plugin.getJobManager().getJobById(jobId);
        if (job == null) return;

        String type = PDCUtil.getStringData(sign, Keys.LEADERBOARD_TYPE);
        if (type == null) return;

        LeaderboardType boardType = CollectionsUtil.getEnum(type, LeaderboardType.class);
        if (boardType == null) return;

        LeaderboardScore score = this.getScore(boardType, job, pos);

        String[] lines = LeaderboardConfig.signsFormat.get(boardType);
        if (lines == null) return;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i]
                .replace(PLACEHOLDER_JOB, job.getName()).replace(PLACEHOLDER_POSITION, String.valueOf(pos))
                .replace(PLACEHOLDER_NAME, score.name()).replace(PLACEHOLDER_SCORE, NumberUtil.format(score.score()));
            sign.setLine(i, line);
        }
        sign.update(true);

        BlockData data = sign.getBlockData();
        if (data instanceof Directional directional) {
            Block signHolder = sign.getBlock().getRelative(directional.getFacing().getOppositeFace());

            Block[] skulls = new Block[2];
            skulls[0] = signHolder.getRelative(BlockFace.UP);
            skulls[1] = sign.getBlock().getRelative(BlockFace.UP);

            // Skull skin on the block that holds Sign
            for (Block skullBlock : skulls) {
                if (skullBlock.getState() instanceof Skull skull) {
                    skull.setOwner(score.getSkullOwner());
                    skull.update(true);
                }
            }
        }
    }
}
