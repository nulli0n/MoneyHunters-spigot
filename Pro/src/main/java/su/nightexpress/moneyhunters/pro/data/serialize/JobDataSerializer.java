package su.nightexpress.moneyhunters.pro.data.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.moneyhunters.pro.MoneyHuntersAPI;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.job.JobState;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;
import su.nightexpress.moneyhunters.pro.data.object.UserObjectiveLimit;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JobDataSerializer implements JsonDeserializer<UserJobData>, JsonSerializer<UserJobData> {

    @Override
    public UserJobData deserialize(JsonElement json, Type type, JsonDeserializationContext contex)
        throws JsonParseException {

        JsonObject object = json.getAsJsonObject();

        String jobId;
        int jobLevel;
        int jobExp;
        if (object.get("id") != null) {
            jobId = object.get("id").getAsString();
            jobLevel = object.get("lvl").getAsInt();
            jobExp = object.get("exp").getAsInt();
        }
        else {
            jobId = object.get("jobId").getAsString();
            jobLevel = object.get("jobLevel").getAsInt();
            jobExp = object.get("jobExp").getAsInt();
        }

        JobState jobState;
        if (object.get("jobState") != null) {
            jobState = CollectionsUtil.getEnum(object.get("jobState").getAsString(), JobState.class);
            if (jobState == null) jobState = JobState.INACTIVE;
        }
        else jobState = JobState.PRIMARY;

        IJob<?> job = MoneyHuntersAPI.getJobById(jobId);
        if (job == null) return null;
		
		/*JsonElement perkObj = j.get("perkLevels");
		Map<String, Integer> perkLevels;
		if (perkObj != null) {
			perkLevels = contex.deserialize(perkObj, new TypeToken<Map<String, Integer>>(){}.getType());
		}
		else {
			perkLevels = new HashMap<>();
		}*/

        JsonElement limitsObj = object.get("dailyLimits");
        if (limitsObj == null) limitsObj = object.get("objectCount");

        Map<String, UserObjectiveLimit> limits;
        if (limitsObj != null) {
            limits = contex.deserialize(limitsObj, new TypeToken<Map<String, UserObjectiveLimit>>() {
            }.getType());
        }
        else limits = new HashMap<>();

        return new UserJobData(job, jobState, jobLevel, jobExp, limits);
    }

    @Override
    public JsonElement serialize(UserJobData src, Type type, JsonSerializationContext contex) {

        JsonObject object = new JsonObject();
        object.addProperty("jobId", src.getJob().getId());
        object.addProperty("jobState", src.getState().name());
        object.addProperty("jobLevel", src.getJobLevel());
        object.addProperty("jobExp", src.getJobExp());
        object.add("perkLevels", contex.serialize(src.getPerkLevels()));
        object.add("dailyLimits", contex.serialize(src.getDailyLimits()));

        return object;
    }
}
