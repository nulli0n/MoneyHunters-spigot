package su.nightexpress.moneyhunters.basic.api.booster;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.MoneyHuntersAPI;
import su.nightexpress.moneyhunters.basic.Placeholders;
import su.nightexpress.moneyhunters.basic.api.job.IJob;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public abstract class AbstractBooster implements IBooster {

    protected MoneyHunters plugin = MoneyHuntersAPI.PLUGIN;
    protected String       id;
    protected Set<String>  jobs;
    protected double       moneyModifier;
    protected double       expModifier;

    public AbstractBooster(@NotNull String id, @NotNull Set<String> jobs, double moneyModifier, double expModifier) {
        this.id = id.toLowerCase();
        this.jobs = jobs.stream().map(String::toLowerCase).collect(Collectors.toCollection(HashSet::new));
        this.moneyModifier = moneyModifier;
        this.expModifier = expModifier;
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        Collection<String> jobs = this.getJobs();
        if (jobs.contains(su.nexmedia.engine.utils.Placeholders.WILDCARD)) {
            jobs.clear();
            jobs.addAll(MoneyHuntersAPI.getJobManager().getJobIds());
        }

        return str -> str
            .replace(Placeholders.BOOSTER_ID, this.getId())
            .replace(Placeholders.BOOSTER_TYPE, plugin.getLangManager().getEnum(this.getType()))
            .replace(Placeholders.BOOSTER_EXP_MODIFIER, NumberUtil.format(this.getExpModifier()))
            .replace(Placeholders.BOOSTER_EXP_MODIFIER_PERCENT, NumberUtil.format(this.getExpPercent()))
            .replace(Placeholders.BOOSTER_MONEY_MODIFIER, NumberUtil.format(this.getMoneyModifier()))
            .replace(Placeholders.BOOSTER_MONEY_MODIFIER_PERCENT, NumberUtil.format(this.getMoneyPercent()))
            .replace(Placeholders.BOOSTER_JOBS, jobs.stream().map(MoneyHuntersAPI::getJobById)
                .filter(Objects::nonNull).map(IJob::getName).collect(Collectors.joining(", ")))
            .replace(Placeholders.BOOSTER_TIME_LEFT, plugin.getMessage(EngineLang.OTHER_INFINITY).getLocalized())
            ;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    @Override
    public Set<String> getJobs() {
        return jobs;
    }

    @Override
    public double getMoneyModifier() {
        return moneyModifier;
    }

    @Override
    public double getExpModifier() {
        return this.expModifier;
    }
}
