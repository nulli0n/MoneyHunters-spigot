package su.nightexpress.moneyhunters.basic.data;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserDataHandler;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLValue;
import su.nexmedia.engine.api.data.sql.column.ColumnType;
import su.nightexpress.moneyhunters.basic.MoneyHunters;
import su.nightexpress.moneyhunters.basic.api.booster.BoosterType;
import su.nightexpress.moneyhunters.basic.api.booster.IBooster;
import su.nightexpress.moneyhunters.basic.config.Config;
import su.nightexpress.moneyhunters.basic.data.object.MoneyUser;
import su.nightexpress.moneyhunters.basic.data.object.UserJobData;
import su.nightexpress.moneyhunters.basic.data.serialize.JobDataSerializer;
import su.nightexpress.moneyhunters.basic.data.serialize.PersonalBoosterSerializer;
import su.nightexpress.moneyhunters.basic.manager.booster.object.PersonalBooster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class MoneyDataHandler extends AbstractUserDataHandler<MoneyHunters, MoneyUser> {

    private static final SQLColumn COLUMN_PROGRESS = SQLColumn.of("progress", ColumnType.STRING);
    private static final SQLColumn COLUMN_BOOSTERS = SQLColumn.of("boosters", ColumnType.STRING);

    private static MoneyDataHandler               instance;
    private final  Function<ResultSet, MoneyUser> userFunction;

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
                    jobData = this.gson.fromJson(resultSet.getString(COLUMN_PROGRESS.getName()), new TypeToken<Map<String, UserJobData>>() {
                    }.getType());
                }

                Set<IBooster> boosters = this.gson.fromJson(resultSet.getString(COLUMN_BOOSTERS.getName()), new TypeToken<Set<PersonalBooster>>() {
                }.getType());

                return new MoneyUser(plugin, uuid, name, dateCreated, lastOnline, jobData, boosters);
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
    @NotNull
    protected List<SQLColumn> getExtraColumns() {
        return Arrays.asList(COLUMN_PROGRESS, COLUMN_BOOSTERS);
    }

    @Override
    @NotNull
    protected List<SQLValue> getSaveColumns(@NotNull MoneyUser user) {
        return Arrays.asList(
            COLUMN_PROGRESS.toValue(this.gson.toJson(user.getJobData())),
            COLUMN_BOOSTERS.toValue(this.gson.toJson(user.getBoosters().stream()
                .filter(booster -> booster.getType() == BoosterType.PERSONAL && !booster.isExpired()).toList()))
        );
    }

    @Override
    @NotNull
    protected Function<ResultSet, MoneyUser> getFunctionToUser() {
        return this.userFunction;
    }
}
