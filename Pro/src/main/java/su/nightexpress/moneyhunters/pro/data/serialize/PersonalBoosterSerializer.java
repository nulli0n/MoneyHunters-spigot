package su.nightexpress.moneyhunters.pro.data.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import su.nightexpress.moneyhunters.pro.manager.booster.object.PersonalBooster;

import java.lang.reflect.Type;
import java.util.Set;

public class PersonalBoosterSerializer implements JsonSerializer<PersonalBooster>, JsonDeserializer<PersonalBooster> {

    @Override
    public PersonalBooster deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();
        if (object.get("id") == null) return null;

        String id = object.get("id").getAsString();
        Set<String> jobs = context.deserialize(object.get("jobs"), new TypeToken<Set<String>>() {
        }.getType());
        double moneyModifier = object.get("moneyModifier").getAsDouble();
        double expModifier = object.get("expModifier").getAsDouble();
        long timeEnd = object.get("timeEnd").getAsLong();

        return new PersonalBooster(id, jobs, moneyModifier, expModifier, timeEnd);
    }

    @Override
    public JsonElement serialize(PersonalBooster booster, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.addProperty("id", booster.getId());
        object.add("jobs", context.serialize(booster.getJobs(), new TypeToken<Set<String>>() {
        }.getType()));
        object.addProperty("moneyModifier", booster.getMoneyModifier());
        object.addProperty("expModifier", booster.getExpModifier());
        object.addProperty("timeEnd", booster.getTimeEnd());

        return object;
    }
}
