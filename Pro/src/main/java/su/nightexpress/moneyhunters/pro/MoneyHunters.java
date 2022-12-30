package su.nightexpress.moneyhunters.pro;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.data.UserDataHolder;
import su.nexmedia.engine.command.list.ReloadSubCommand;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.playerblocktracker.PlayerBlockTracker;
import su.nightexpress.moneyhunters.pro.api.job.JobState;
import su.nightexpress.moneyhunters.pro.api.job.JobType;
import su.nightexpress.moneyhunters.pro.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.pro.command.base.*;
import su.nightexpress.moneyhunters.pro.command.booster.BoosterCommand;
import su.nightexpress.moneyhunters.pro.config.Config;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.data.MoneyDataHandler;
import su.nightexpress.moneyhunters.pro.data.MoneyUserManager;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.hooks.external.PlaceholderHook;
import su.nightexpress.moneyhunters.pro.manager.booster.BoosterManager;
import su.nightexpress.moneyhunters.pro.manager.currency.CurrencyManager;
import su.nightexpress.moneyhunters.pro.manager.job.JobManager;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardManager;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardType;
import su.nightexpress.moneyhunters.pro.manager.money.MoneyManager;

import java.sql.SQLException;

public class MoneyHunters extends NexPlugin<MoneyHunters> implements UserDataHolder<MoneyHunters, MoneyUser> {

    private MoneyDataHandler dataHandler;
    private MoneyUserManager userManager;

    private CurrencyManager    currencyManager;
    private BoosterManager     boosterManager;
    private JobManager         jobManager;
    private MoneyManager       moneyManager;
    private LeaderboardManager leaderboardManager;

    @Override
    @NotNull
    protected MoneyHunters getSelf() {
        return this;
    }

    @Override
    public void enable() {
        this.currencyManager = new CurrencyManager(this);
        this.currencyManager.setup();
        if (!this.currencyManager.hasCurrency()) {
            this.error("No currencies are available! Plugin will be disabled.");
            this.getPluginManager().disablePlugin(this);
            return;
        }

        this.boosterManager = new BoosterManager(this);
        this.boosterManager.setup();

        PlayerBlockTracker.initialize(this);

        this.jobManager = new JobManager(this);
        this.jobManager.setup();

        this.moneyManager = new MoneyManager(this);
        this.moneyManager.setup();

        if (Config.LEADERBOARDS_ENABLED) {
            this.leaderboardManager = new LeaderboardManager(this);
            this.leaderboardManager.setup();
        }

        this.info("Thank you for using the Pro version <3 !");
    }

    @Override
    public void disable() {
        if (this.boosterManager != null) {
            this.boosterManager.shutdown();
            this.boosterManager = null;
        }
        if (this.jobManager != null) {
            this.jobManager.shutdown();
            this.jobManager = null;
        }
        if (this.moneyManager != null) {
            this.moneyManager.shutdown();
            this.moneyManager = null;
        }
        if (this.leaderboardManager != null) {
            this.leaderboardManager.shutdown();
            this.leaderboardManager = null;
        }
        if (this.currencyManager != null) {
            this.currencyManager.shutdown();
            this.currencyManager = null;
        }
    }

    @Override
    public void loadConfig() {
        Config.load(this);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(Lang.class);
        this.getLangManager().setupEnum(JobState.class);
        this.getLangManager().setupEnum(JobType.class);
        this.getLangManager().setupEnum(LeaderboardType.class);
        this.getLangManager().setupEnum(ObjectiveLimitType.class);
        this.getLang().saveChanges();
    }

    @Override
    public boolean setupDataHandlers() {
        try {
            this.dataHandler = MoneyDataHandler.getInstance(this);
            this.dataHandler.setup();
        }
        catch (SQLException ex) {
            this.error("Could not setup data handler!");
            ex.printStackTrace();
            return false;
        }

        this.userManager = new MoneyUserManager(this);
        this.userManager.setup();

        return true;
    }

    @Override
    public void registerHooks() {
        if (Hooks.hasPlaceholderAPI()) {
            PlaceholderHook.setup();
        }
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<MoneyHunters> mainCommand) {
        if (Config.LEVELING_ENABLED) {
            mainCommand.addChildren(new StatsCommand(this));
            mainCommand.addChildren(new ExpCommand(this));
            mainCommand.addChildren(new LevelCommand(this));
            mainCommand.addChildren(new ResetCommand(this));
        }
        if (Config.JOBS_COMMAND_AS_DEFAULT) {
            mainCommand.addDefaultCommand(new JobsCommand(this));
        }
        else {
            mainCommand.addChildren(new JobsCommand(this));
        }
        mainCommand.addChildren(new ReloadSubCommand<>(this, "moneyhunters.admin"));
        mainCommand.addChildren(new InfoCommand(this));
        mainCommand.addChildren(new DropCommand(this));
        mainCommand.addChildren(new BoosterCommand(this));
        mainCommand.addChildren(new ObjectivesCommand(this));
        mainCommand.addChildren(new SoundCommand(this));
        mainCommand.removeChildren("about");
    }

    @Override
    public void registerPermissions() {
        // TODO
    }

    @Override
    @NotNull
    public MoneyDataHandler getData() {
        return this.dataHandler;
    }

    @NotNull
    @Override
    public MoneyUserManager getUserManager() {
        return userManager;
    }

    @NotNull
    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    @NotNull
    public BoosterManager getBoosterManager() {
        return boosterManager;
    }

    @Nullable
    public LeaderboardManager getLeaderboardManager() {
        return this.leaderboardManager;
    }

    @NotNull
    public JobManager getJobManager() {
        return this.jobManager;
    }

    @NotNull
    public MoneyManager getMoneyManager() {
        return this.moneyManager;
    }
}
