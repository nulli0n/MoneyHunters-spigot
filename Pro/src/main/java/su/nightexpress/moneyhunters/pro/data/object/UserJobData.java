package su.nightexpress.moneyhunters.pro.data.object;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.booster.IBooster;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.job.JobState;
import su.nightexpress.moneyhunters.pro.api.money.IMoneyObjective;
import su.nightexpress.moneyhunters.pro.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.pro.config.Config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class UserJobData {

    private transient final IJob<?> job;
    private transient       double  moneyModifier;

    private JobState state;
    private int      jobLevel;
    private int      jobExp;
    private int      jobExpMax;

    private final Map<String, Integer>            perkLevels;
    private final Map<String, UserObjectiveLimit> dailyLimits;

    public UserJobData(@NotNull IJob<?> job) {
        this(job, job.getStateDefault(), job.getLevelStart(), 0, new HashMap<>());
    }

    public UserJobData(@NotNull IJob<?> job,
                       @NotNull JobState state,
                       int jobLevel, int jobExp,
                       @NotNull Map<String, UserObjectiveLimit> dailyLimits) {
        this.job = job;
        this.setState(state);
        this.dailyLimits = dailyLimits;
        this.perkLevels = new HashMap<>();

        if (Config.LEVELING_ENABLED) {
            this.jobLevel = jobLevel;
            this.jobExp = jobExp;
            this.update();
        }
        else {
            this.jobLevel = 1;
            this.jobExp = 0;
            this.jobExpMax = 0;
            this.moneyModifier = 1D;
        }
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders(@NotNull MoneyUser user) {
        Collection<IBooster> boosters = user.getBoosters(this.getJob());
        double boostExp = boosters.stream().mapToDouble(IBooster::getExpPercent).sum();
        double boostMoney = boosters.stream().mapToDouble(IBooster::getMoneyPercent).sum();

        return str -> this.replacePlaceholders().apply(str
            .replace(Placeholders.JOB_BOOSTER_EXP, NumberUtil.format(boostExp))
            .replace(Placeholders.JOB_BOOSTER_MONEY, NumberUtil.format(boostMoney))
        );
    }


    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        IJob<?> job = this.getJob();
        return str -> job.replacePlaceholders().apply(str
            .replace(Placeholders.JOB_STATE, job.plugin().lang().getEnum(this.getState()))
            .replace(Placeholders.JOB_EXP, NumberUtil.format(this.getJobExp()))
            .replace(Placeholders.JOB_EXP_MAX, NumberUtil.format(this.getJobExpMax()))
            .replace(Placeholders.JOB_LEVEL, NumberUtil.format(this.getJobLevel()))
            .replace(Placeholders.JOB_LEVEL_MAX, NumberUtil.format(this.getJobLevelMax()))
            .replace(Placeholders.JOB_MONEY_MODIFIER, NumberUtil.format(this.getMoneyModifier()))
        );
    }


    public void reset() {
        IJob<?> job = this.getJob();
        this.setJobLevel(job.getLevelStart());
        this.setJobExp(0);
        this.setState(job.getStateDefault());
        this.getPerkLevels().clear();

        this.update();
    }


    public void update() {
        if (!Config.LEVELING_ENABLED) return;

        this.jobExpMax = this.job.getExpForLevel(this.getJobLevel());
        this.moneyModifier = this.job.getMoneyMultiplier(this.getState(), this.getJobLevel());

        if (this.getJobExp() >= this.getJobExpMax() && this.getJobLevel() < this.getJobLevelMax()) {
            this.upLevel(this.getJobExp() - this.getJobExpMax());
        }
    }


    @NotNull
    public IJob<?> getJob() {
        return this.job;
    }

    @NotNull
    public JobState getState() {
        return state;
    }

    public void setState(@NotNull JobState state) {
        this.state = state;
    }

    public int getJobLevel() {
        return this.jobLevel;
    }


    public void setJobLevel(int jobLevel) {
        this.jobLevel = Math.max(1, Math.min(jobLevel, this.getJobLevelMax()));
    }

    public int getJobLevelMax() {
        return this.getJob().getLevelMax(this.getState());
    }

    public int getJobExp() {
        return this.jobExp;
    }


    public void setJobExp(int jobExp) {
        this.jobExp = jobExp;
    }


    public int getJobExpMax() {
        return this.jobExpMax;
    }


    public void setJobExpMax(int jobExpMax) {
        this.jobExpMax = jobExpMax;
    }


    public final int getJobExpToUp() {
        return this.getJobExpMax() - this.getJobExp();
    }


    public final int getJobExpToDown() {
        return -(this.getJobExp() + this.getJobExpMax());
    }


    public double getMoneyModifier() {
        return this.moneyModifier;
    }


    @NotNull
    public Map<String, Integer> getPerkLevels() {
        return perkLevels;
    }


    public int getPerkLevel(@NotNull String id) {
        return this.perkLevels.getOrDefault(id.toLowerCase(), 0);
    }


    public void setPerkLevel(@NotNull String id, int level) {
        this.perkLevels.put(id.toLowerCase(), level);
    }

    @NotNull

    public Map<String, UserObjectiveLimit> getDailyLimits() {
        return dailyLimits;
    }


    @NotNull
    public UserObjectiveLimit getObjectiveLimit(@NotNull String objectId) {
        return this.dailyLimits.computeIfAbsent(objectId.toLowerCase(), counter -> new UserObjectiveLimit());
    }


    public boolean isObjectWasted(@NotNull IMoneyObjective objective, @NotNull ObjectiveLimitType type) {
        UserObjectiveLimit limit = this.getObjectiveLimit(objective.getType());
        limit.validateTime();
        return objective.isDailyLimited(type, this.getJobLevel()) && objective.getDailyLimit(type, this.getJobLevel()) <= limit.getCount(type);
    }

    public void takeExp(int expAdd) {
        this.addExp(-expAdd);
    }

    public void addExp(int expAdd) {
        //if (expAdd == 0) return;

        int expHas = this.getJobExp();
        int expMax = this.getJobExpMax();
        int expToDown = this.getJobExpToDown();

        if (expAdd <= expToDown) {
            if (this.getJobLevel() == this.getJob().getLevelStart()) {
                this.setJobExp(-expMax);
            }
            else {
                int expLeft = Math.abs(expAdd) - Math.abs(expToDown);
                this.downLevel(expLeft);
            }
            return;
        }

        if (expHas + expAdd < expMax) {
            this.setJobExp(expHas + expAdd);
        }
        else {
            if (this.getJobLevel() >= this.getJobLevelMax()) {
                this.setJobExp(this.getJobExpMax());
            }
            else {
                this.upLevel((expHas + expAdd) - expMax);
            }
        }
    }


    public void upLevel(int expLeft) {
        this.jobLevel += 1;
        this.moneyModifier = this.job.getMoneyMultiplier(this.state, this.jobLevel);

        int expReq = this.getJob().getExpForLevel(this.getJobLevel());
        if (expReq <= 0) expReq = this.getJob().getLevelExpStart();

        this.jobExp = expLeft;
        this.jobExpMax = expReq;

        if (expLeft >= expReq) {
            if (this.getJobLevel() >= this.getJobLevelMax()) {
                this.addExp(1);
            }
            else {
                this.upLevel(expLeft - expReq);
            }
        }
    }


    public void downLevel(int expLeft) {
        if (this.getJobLevel() == this.getJob().getLevelStart()) return;

        int expMax = this.getJob().getExpForLevel(this.getJobLevel() - 1);
        if (expMax <= 0) expMax = this.getJob().getLevelExpStart();

        this.jobExp = (-Math.abs(expLeft));
        this.jobExpMax = expMax;
        this.jobLevel -= 1;
        this.moneyModifier = this.job.getMoneyMultiplier(this.state, this.jobLevel);

        int expDown = -(this.getJobExpMax());
        if (this.getJobExp() <= expDown) {
            if (this.getJobLevel() == this.getJob().getLevelStart()) {
                this.setJobExp(this.getJobExpToDown());
            }
            else {
                this.downLevel((this.getJobExp() - expDown));
            }
        }
    }
}
