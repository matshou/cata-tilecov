package io.matshou.cata.tilecov.json;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * This deserializer is used to deserialize {@link TileAtlasJsonObject} from JSON.
 */
public class TileAtlasJsonDeserializer extends JsonObjectDeserializer<TileAtlasJsonObject> {

	TileAtlasJsonDeserializer() {
		super(TileAtlasJsonObject.class);
	}

	@Override
	void deserializeObjectMembers(Gson gson, JsonObject object, TileAtlasJsonObject target, String entry) {

		JsonElement element = object.get(entry);
		if (element == null) {
			return;
		}
		Optional<List<TilesJsonObject>> tileAtlasJsonObjects = JsonObjectBuilder.<TilesJsonObject>create()
				.ofType(TilesJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.withDeserializer(TilesJsonDeserializer.class)
				.buildAsList(element.toString());

		Field field = Objects.requireNonNull(jsonObjectFields.get("tiles"));
		if (tileAtlasJsonObjects.isEmpty()) {
			throw new NullJsonObjectException("tiles", TilesJsonObject.class);
		}
		changeFieldValue(field, target, tileAtlasJsonObjects.get());
	}
}
