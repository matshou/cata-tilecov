package io.matshou.cata.tilecov.json;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * This class represents a custom deserializer for {@link CataJsonObject} class.
 */
public class CataJsonDeserializer extends JsonObjectDeserializer<CataJsonObject> {

	CataJsonDeserializer() {
		super(CataJsonObject.class);
	}

	@Override
	void deserializeObjectMembers(Gson gson, JsonObject object, CataJsonObject target, String entry) {

		JsonElement element = object.get(entry);
		if (element == null) {
			return;
		}
		Field field = Objects.requireNonNull(jsonObjectFields.get("name"));
		if (element.isJsonPrimitive()) {
			changeFieldValue(field, target, new CataJsonObjectName(element.getAsString()));
			return;
		}
		Optional<CataJsonObjectName> jsonObjectName = JsonObjectBuilder.<CataJsonObjectName>create()
				.ofType(CataJsonObjectName.class)
				.withTypeToken(new TypeToken<>() {})
				.build(element.toString());

		if (jsonObjectName.isEmpty()) {
			throw new NullJsonObjectException("name", TilesJsonObject.class);
		}
		changeFieldValue(field, target, jsonObjectName.get());
	}
}
