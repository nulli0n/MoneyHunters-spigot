package su.nightexpress.moneyhunters.pro.manager.booster.object;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.booster.AbstractBooster;
import su.nightexpress.moneyhunters.pro.api.booster.BoosterType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class AutoBooster extends AbstractBooster {

    private final Set<DayOfWeek>   days;
    private final Set<LocalTime[]> times;

    public AutoBooster(
        @NotNull Set<DayOfWeek> days, @NotNull Set<LocalTime[]> times,
        @NotNull Set<String> jobs, double moneyModifier, double expModifier) {
        super(UUID.randomUUID().toString(), jobs, moneyModifier, expModifier);
        this.days = days;
        this.times = times;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        UnaryOperator<String> replacer = super.replacePlaceholders();

        LocalTime[] times = this.getCurrentTimes();
        if (times == null) return replacer;

        LocalDateTime date = LocalDateTime.of(LocalDate.now(), times[1]);
        return str -> replacer.apply(str
            .replace(Placeholders.BOOSTER_TIME_LEFT, TimeUtil.formatTimeLeft(TimeUtil.toEpochMillis(date)))
        );
    }

    @Override
    @NotNull
    public BoosterType getType() {
        return BoosterType.AUTO;
    }

    @Override
    public boolean isExpired() {
        return this.isAwaiting();
    }

    @Override
    public boolean isAwaiting() {
        return this.getCurrentTimes() == null;
    }

    @Nullable
    public LocalTime[] getCurrentTimes() {
        LocalDateTime date = LocalDateTime.now();
        DayOfWeek day = date.getDayOfWeek();
        if (!this.getDays().contains(day)) return null;

        LocalTime timeNow = LocalTime.now();
        return this.getTimes().stream()
            .filter(times -> timeNow.isAfter(times[0]) && timeNow.isBefore(times[1]))
            .findFirst().orElse(null);
    }

    @NotNull
    public Set<DayOfWeek> getDays() {
        return this.days;
    }

    @NotNull
    public Set<LocalTime[]> getTimes() {
        return this.times;
    }

    @NotNull
    public static Set<LocalTime[]> parseTimes(@NotNull List<String> list) {
        Set<LocalTime[]> times = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

        list.forEach(timeRaw -> {
            String[] split = timeRaw.split("-");
            if (split.length < 2) return;

            LocalTime start = LocalTime.parse(split[0], formatter);
            LocalTime end = LocalTime.parse(split[1], formatter);
            times.add(new LocalTime[]{start, end});
        });
        return times;
    }

    @NotNull
    public static Set<DayOfWeek> parseDays(@NotNull String str) {
        Set<DayOfWeek> days = new HashSet<>();
        for (String split : str.split(",")) {
            DayOfWeek day = CollectionsUtil.getEnum(split.trim(), DayOfWeek.class);
            if (day != null) days.add(day);
        }
        return days;
    }
}
