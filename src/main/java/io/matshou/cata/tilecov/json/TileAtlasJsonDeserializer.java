package io.matshou.cata.tilecov.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * This deserializer is used to deserialize {@link TileAtlasJsonObject} from JSON.
 */
public class TileAtlasJsonDeserializer extends JsonObjectDeserializer<TileAtlasJsonObject> {

	TileAtlasJsonDeserializer() {
		super(TileAtlasJsonObject.class);
	}

	@Override
	void deserializeObjectMembers(Gson gson, JsonObject jsonObject, TileAtlasJsonObject target, String entry) {

		JsonElement element = jsonObject.get(entry);
		if (element == null) {
			return;
		}
		List<TilesJsonObject> tileAtlasJsonObjects = JsonObjectBuilder.<TilesJsonObject>create()
				.ofType(TilesJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.withDeserializer(TilesJsonDeserializer.class)
				.buildAsList(element.toString());

		Field field = Objects.requireNonNull(jsonObjectFields.get("tiles"));
		changeFieldValue(field, target, tileAtlasJsonObjects);
	}
}
