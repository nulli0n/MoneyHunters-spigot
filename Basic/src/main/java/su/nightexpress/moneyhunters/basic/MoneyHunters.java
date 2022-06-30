package su.nightexpress.moneyhunters.basic;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.data.UserDataHolder;
import su.nexmedia.engine.manager.player.blocktracker.PlayerBlockTracker;
import su.nightexpress.moneyhunters.basic.api.booster.BoosterType;
import su.nightexpress.moneyhunters.basic.api.job.JobState;
import su.nightexpress.moneyhunters.basic.api.job.JobType;
import su.nightexpress.moneyhunters.basic.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.basic.command.base.*;
import su.nightexpress.moneyhunters.basic.command.booster.BoosterCommand;
import su.nightexpress.moneyhunters.basic.config.Config;
import su.nightexpress.moneyhunters.basic.config.Lang;
import su.nightexpress.moneyhunters.basic.data.MoneyDataHandler;
import su.nightexpress.moneyhunters.basic.data.MoneyUserManager;
import su.nightexpress.moneyhunters.basic.data.object.MoneyUser;
import su.nightexpress.moneyhunters.basic.manager.booster.BoosterManager;
import su.nightexpress.moneyhunters.basic.manager.currency.CurrencyManager;
import su.nightexpress.moneyhunters.basic.manager.job.JobManager;
import su.nightexpress.moneyhunters.basic.manager.money.MoneyManager;

import java.sql.SQLException;

public class MoneyHunters extends NexPlugin<MoneyHunters> implements UserDataHolder<MoneyHunters, MoneyUser> {

    private MoneyDataHandler dataHandler;
    private MoneyUserManager userManager;

    private CurrencyManager currencyManager;
    private BoosterManager  boosterManager;
    private JobManager      jobManager;
    private MoneyManager    moneyManager;

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

        PlayerBlockTracker.initialize();

        this.jobManager = new JobManager(this);
        this.jobManager.setup();

        this.moneyManager = new MoneyManager(this);
        this.moneyManager.setup();
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
        this.getLangManager().setupEnum(ObjectiveLimitType.class);
        this.getLangManager().setupEnum(BoosterType.class);
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

    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<MoneyHunters> mainCommand) {
        if (Config.LEVELING_ENABLED) {
            mainCommand.addChildren(new StatsCommand(this));
            mainCommand.addChildren(new ExpCommand(this));
            mainCommand.addChildren(new LevelCommand(this));
            mainCommand.addChildren(new ResetCommand(this));
        }
        mainCommand.addChildren(new InfoCommand(this));
        mainCommand.addChildren(new BoosterCommand(this));
        mainCommand.removeChildren("about");
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

    @NotNull
    public JobManager getJobManager() {
        return this.jobManager;
    }

    @NotNull
    public MoneyManager getMoneyManager() {
        return this.moneyManager;
    }
}
