package su.nightexpress.moneyhunters.pro.config;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nexmedia.engine.hooks.external.WorldGuardHook;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.job.JobState;
import su.nightexpress.moneyhunters.pro.hooks.HookId;
import su.nightexpress.moneyhunters.pro.manager.booster.object.AutoBooster;
import su.nightexpress.moneyhunters.pro.manager.booster.object.RankBooster;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class Config {

    public static boolean LEADERBOARDS_ENABLED;

    public static boolean                             JOBS_COMMAND_AS_DEFAULT;
    public static Map<JobState, Map<String, Integer>> JOBS_STATE_MAX_AMOUNT_BY_RANK;
    public static final JOption<Integer> JOBS_STATE_CHANGE_COOLDOWN = JOption.create("Jobs.State.Change_Cooldown", 3600,
        "Sets the cooldown (in seconds) to change state of the same job.");

    public static Set<String> GEN_GLITCH_IGNORE_SPAWN_REASONS;
    public static Set<String> GEN_GLITCH_IGNORE_GAME_MODES;
    public static Set<String> GEN_GLITCH_IGNORE_BLOCK_GENERATORS;

    public static final JOption<Set<String>> GLITCH_IGNORE_BLOCK_FERTILIZE = JOption.create("Generic.Glitches.Ignore_Block_Fertilize",
        Set.of(Material.WHEAT.name(), Material.CARROTS.name(), Material.POTATOES.name(), Material.BEETROOTS.name()),
        "A list of blocks that will drop no money if been fertilized by bone meal.");

    public static Set<String> GEN_DISABLED_WORLDS;
    public static boolean     GEN_HOOKS_DISABLED_ON_MOB_ARENA;
    public static Set<String> GEN_HOOKS_DISABLED_WG_REGIONS;

    public static boolean LEVELING_ENABLED;
    public static boolean LEVELING_LEVEUP_FIREWORK;

    public static  int              BOOSTERS_UPDATE_INTERVAL;
    public static  boolean          BOOSTERS_NOTIFY_ON_JOIN;
    public static  int              BOOSTERS_NOTIFY_INTERVAL;
    private static Set<AutoBooster> BOOSTERS_AUTO;
    private static Set<RankBooster> BOOSTERS_RANK;

    public static boolean MONEY_OWNER_PROTECTION_ENABLED;
    public static boolean MONEY_MERGING_ENABLED;
    public static long    MONEY_FULL_INVENTORY_TASK_INTERVAL = 0;

    public static void load(@NotNull MoneyHunters plugin) {
        JYML cfg = plugin.getConfig();

        String path = "Leaderboards.";
        LEADERBOARDS_ENABLED = cfg.getBoolean(path + "Enabled", true);

        path = "Jobs.";
        for (JobState state : JobState.values()) {
            cfg.addMissing(path + "State.Max_Amount_By_Rank." + state.name() + ".default", -1);
        }

        JOBS_COMMAND_AS_DEFAULT = cfg.getBoolean(path + "Inject_Jobs_Command_As_Default");
        JOBS_STATE_MAX_AMOUNT_BY_RANK = new HashMap<>();
        for (JobState state : JobState.values()) {
            Map<String, Integer> mapRank = new HashMap<>();
            for (String sRank : cfg.getSection(path + "State.Max_Amount_By_Rank." + state.name())) {
                String path2 = path + "State.Max_Amount_By_Rank." + state.name() + "." + sRank;
                int max = cfg.getInt(path2);
                mapRank.put(sRank.toLowerCase(), max);
            }
            JOBS_STATE_MAX_AMOUNT_BY_RANK.put(state, mapRank);
        }


        path = "Generic.Glitches.";
        cfg.addMissing(path + "Ignore_Block_Generators", Arrays.asList("STONE", "COBBLESTONE", "OBSIDIAN"));

        GEN_GLITCH_IGNORE_SPAWN_REASONS = cfg.getStringSet(path + "Ignore_Spawn_Reasons")
            .stream().map(String::toUpperCase).collect(Collectors.toSet());
        GEN_GLITCH_IGNORE_GAME_MODES = cfg.getStringSet(path + "Ignore_Game_Modes")
            .stream().map(String::toUpperCase).collect(Collectors.toSet());
        GEN_GLITCH_IGNORE_BLOCK_GENERATORS = cfg.getStringSet(path + "Ignore_Block_Generators")
            .stream().map(String::toUpperCase).collect(Collectors.toSet());

        path = "Generic.";
        GEN_DISABLED_WORLDS = cfg.getStringSet(path + "Disabled_Worlds");

        path = "Generic.Hooks.";
        GEN_HOOKS_DISABLED_ON_MOB_ARENA = cfg.getBoolean(path + "Disabled_In_AdvancedMobArena") && Hooks.hasPlugin(HookId.AMA);
        if (Hooks.hasPlugin(Hooks.WORLD_GUARD)) {
            GEN_HOOKS_DISABLED_WG_REGIONS = cfg.getStringSet(path + "Disabled_WorldGuard_Regions")
                .stream().map(String::toLowerCase).collect(Collectors.toSet());
        }


        path = "Leveling.";
        if (LEVELING_ENABLED = cfg.getBoolean(path + "Enabled")) {
            LEVELING_LEVEUP_FIREWORK = cfg.getBoolean(path + "Level_Up.Firework");
        }

        path = "Boosters.";
        cfg.addMissing(path + "Update_Interval", 60);
        cfg.addMissing(path + "Notify.On_Join", true);
        cfg.addMissing(path + "Notify.Interval", 900);

        BOOSTERS_UPDATE_INTERVAL = cfg.getInt(path + "Update_Interval", 60);
        BOOSTERS_NOTIFY_ON_JOIN = cfg.getBoolean(path + "Notify.On_Join");
        BOOSTERS_NOTIFY_INTERVAL = cfg.getInt(path + "Notify.Interval", 900);

        BOOSTERS_AUTO = new HashSet<>();
        BOOSTERS_RANK = new HashSet<>();
        for (String bId : cfg.getSection(path + "Global")) {
            String path2 = path + "Global." + bId + ".";

            Set<DayOfWeek> bDays = AutoBooster.parseDays(cfg.getString(path2 + "Times.Days", ""));
            Set<LocalTime[]> bTimes = AutoBooster.parseTimes(cfg.getStringList(path2 + "Times.Times"));

            Set<String> bJobs = cfg.getStringSet(path2 + "Jobs");
            double bMultExp = cfg.getDouble(path2 + "Multiplier.Exp", 1D);
            double bMultMoney = cfg.getDouble(path2 + "Multiplier.Money", 1D);

            AutoBooster boost = new AutoBooster(bDays, bTimes, bJobs, bMultMoney, bMultExp);
            BOOSTERS_AUTO.add(boost);
        }
        for (String bId : cfg.getSection(path + "Rank")) {
            String path2 = path + "Rank." + bId + ".";

            int priority = cfg.getInt(path2 + "Priority");
            String rank = cfg.getString(path2 + "Rank", "");
            if (rank.isEmpty()) continue;

            Set<String> bJobs = cfg.getStringSet(path2 + "Jobs");
            double bMultExp = cfg.getDouble(path2 + "Multiplier.Exp", 1D);
            double bMultMoney = cfg.getDouble(path2 + "Multiplier.Money", 1D);

            RankBooster boost = new RankBooster(bJobs, bMultMoney, bMultExp, priority, rank);
            BOOSTERS_RANK.add(boost);
        }

        path = "Money.Owner_Protection.";
        MONEY_OWNER_PROTECTION_ENABLED = cfg.getBoolean(path + "Enabled");

        path = "Money.Merging.";
        MONEY_MERGING_ENABLED = cfg.getBoolean(path + "Enabled");

        path = "Money.Full_Inventory.";
        if (cfg.getBoolean(path + "Enable_Bypass")) {
            MONEY_FULL_INVENTORY_TASK_INTERVAL = cfg.getInt(path + "Task_Tick_Interval", 40);
        }
    }

    public static boolean isDisabledRegion(@NotNull Player player) {
        if (!Hooks.hasWorldGuard()) return false;
        return Config.GEN_HOOKS_DISABLED_WG_REGIONS.contains(WorldGuardHook.getRegion(player));
    }

    @NotNull
    public static Collection<AutoBooster> getBoosters() {
        return Config.BOOSTERS_AUTO;
    }

    @Nullable
    public static RankBooster getBoosterRank(@NotNull Player player) {
        return BOOSTERS_RANK.stream()
            .filter(booster -> VaultHook.getPermissionGroups(player).contains(booster.getRank()))
            .max(Comparator.comparingInt(RankBooster::getPriority)).orElse(null);
    }
}
