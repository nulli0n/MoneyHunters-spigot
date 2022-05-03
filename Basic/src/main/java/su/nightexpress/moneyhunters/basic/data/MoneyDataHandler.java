package su.nightexpress.moneyhunters.basic.data;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.AbstractUserDataHandler;
import su.nexmedia.engine.api.data.DataTypes;
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

    private static MoneyDataHandler               instance;
    private final  Function<ResultSet, MoneyUser> userFunction;

    protected MoneyDataHandler(@NotNull MoneyHunters plugin) throws SQLException {
        super(plugin);

        this.userFunction = (rs) -> {
            try {
                UUID uuid = UUID.fromString(rs.getString(AbstractUserDataHandler.COL_USER_UUID));
                String name = rs.getString(AbstractUserDataHandler.COL_USER_NAME);
                long lastOnline = rs.getLong(AbstractUserDataHandler.COL_USER_LAST_ONLINE);

                Map<String, UserJobData> jobData = new HashMap<>();
                if (Config.LEVELING_ENABLED) {
                    jobData = this.gson.fromJson(rs.getString("progress"), new TypeToken<Map<String, UserJobData>>() {
                    }.getType());
                }

                Set<IBooster> boosters = this.gson.fromJson(rs.getString("boosters"), new TypeToken<Set<PersonalBooster>>() {
                }.getType());

                return new MoneyUser(plugin, uuid, name, lastOnline, jobData, boosters);
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
    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        return super.registerAdapters(builder
            .registerTypeAdapter(UserJobData.class, new JobDataSerializer())
            .registerTypeAdapter(PersonalBooster.class, new PersonalBoosterSerializer())
        );
    }

    @Override
    protected void onTableCreate() {
        this.addColumn(this.tableUsers, "boosters", DataTypes.STRING.build(this.dataType), "[]");

        super.onTableCreate();
    }

    @Override
    @NotNull
    protected LinkedHashMap<String, String> getColumnsToCreate() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("progress", DataTypes.STRING.build(this.dataType));
        map.put("boosters", DataTypes.STRING.build(this.dataType));
        return map;
    }

    @Override
    @NotNull
    protected LinkedHashMap<String, String> getColumnsToSave(@NotNull MoneyUser user) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("progress", this.gson.toJson(user.getJobData()));
        map.put("boosters", this.gson.toJson(user.getBoosters().stream()
            .filter(booster -> booster.getType() == BoosterType.PERSONAL && !booster.isExpired()).toList()));
        return map;
    }

    @Override
    @NotNull
    protected Function<ResultSet, MoneyUser> getFunctionToUser() {
        return this.userFunction;
    }
}
