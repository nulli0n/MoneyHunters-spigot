package su.nightexpress.moneyhunters.pro.data;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserDataHandler;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLValue;
import su.nexmedia.engine.api.data.sql.column.ColumnType;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.booster.BoosterType;
import su.nightexpress.moneyhunters.pro.api.booster.IBooster;
import su.nightexpress.moneyhunters.pro.config.Config;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;
import su.nightexpress.moneyhunters.pro.data.object.UserSettings;
import su.nightexpress.moneyhunters.pro.data.serialize.JobDataSerializer;
import su.nightexpress.moneyhunters.pro.data.serialize.PersonalBoosterSerializer;
import su.nightexpress.moneyhunters.pro.manager.booster.object.PersonalBooster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class MoneyDataHandler extends AbstractUserDataHandler<MoneyHunters, MoneyUser> {

    private static MoneyDataHandler               instance;
    private final  Function<ResultSet, MoneyUser> userFunction;

    private static final SQLColumn COL_PROGRESS       = SQLColumn.of("progress", ColumnType.STRING);
    private static final SQLColumn    COL_STATE_COOLDOWN = SQLColumn.of("stateCooldown", ColumnType.STRING);
    private static final SQLColumn COL_BOOSTERS = SQLColumn.of("boosters", ColumnType.STRING);
    private static final SQLColumn COL_SETTINGS = SQLColumn.of("settings", ColumnType.STRING);

    protected MoneyDataHandler(@NotNull MoneyHunters plugin) {
        super(plugin, plugin);

        this.userFunction = (resultSet) -> {
            try {
                UUID uuid = UUID.fromString(resultSet.getString(COLUMN_USER_ID.getName()));
                String name = resultSet.getString(COLUMN_USER_NAME.getName());
                long dateCreated = resultSet.getLong(COLUMN_USER_DATE_CREATED.getName());
                long lastOnline = resultSet.getLong(COLUMN_USER_LAST_ONLINE.getName());

                Map<String, UserJobData> jobData = new HashMap<>();
                if (Config.LEVELING_ENABLED) {
                    jobData = this.gson.fromJson(resultSet.getString(COL_PROGRESS.getName()), new TypeToken<Map<String, UserJobData>>() {}.getType());
                }

                Map<String, Long> stateCooldowns = this.gson.fromJson(resultSet.getString(COL_STATE_COOLDOWN.getName()), new TypeToken<Map<String, Long>>(){}.getType());
                Set<IBooster> boosters = this.gson.fromJson(resultSet.getString(COL_BOOSTERS.getName()), new TypeToken<Set<PersonalBooster>>() {}.getType());
                UserSettings settings = this.gson.fromJson(resultSet.getString(COL_SETTINGS.getName()), new TypeToken<UserSettings>(){}.getType());

                return new MoneyUser(plugin, uuid, name, dateCreated, lastOnline, jobData, stateCooldowns, boosters, settings);
            }
            catch (SQLException ex) {
                return null;
            }
        };
    }

    public static MoneyDataHandler getInstance(@NotNull MoneyHunters plugin) throws SQLException {
        if (instance == null) {
            instance = new MoneyDataHandler(plugin);
        }
        return instance;
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
        instance = null;
    }

    @Override
    public void onSynchronize() {
        // TODO
    }

    @Override
    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        return super.registerAdapters(builder
            .registerTypeAdapter(UserJobData.class, new JobDataSerializer())
            .registerTypeAdapter(PersonalBooster.class, new PersonalBoosterSerializer())
        );
    }

    @Override
    protected void createUserTable() {
        super.createUserTable();

        this.addColumn(this.tableUsers,
            COL_BOOSTERS.toValue("[]"),
            COL_SETTINGS.toValue("{}"),
            COL_STATE_COOLDOWN.toValue("{}")
        );

    }

    @Override
    @NotNull
    protected List<SQLColumn> getExtraColumns() {
        return Arrays.asList(
            COL_PROGRESS, COL_SETTINGS, COL_BOOSTERS, COL_STATE_COOLDOWN
        );
    }

    @Override
    @NotNull
    protected List<SQLValue> getSaveColumns(@NotNull MoneyUser user) {
        return Arrays.asList(
            COL_PROGRESS.toValue(this.gson.toJson(user.getJobData())),
            COL_STATE_COOLDOWN.toValue(this.gson.toJson(user.getJobStateCooldowns())),
            COL_BOOSTERS.toValue(this.gson.toJson(user.getBoosters().stream()
                .filter(booster -> booster.getType() == BoosterType.PERSONAL && !booster.isExpired()).toList())),
            COL_SETTINGS.toValue(this.gson.toJson(user.getSettings()))
        );
    }

    @Override
    @NotNull
    protected Function<ResultSet, MoneyUser> getFunctionToUser() {
        return this.userFunction;
    }
}
