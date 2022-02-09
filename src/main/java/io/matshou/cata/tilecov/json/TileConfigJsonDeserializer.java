/*
 * Cata-Tilecov - Generates tile coverage reports for Cataclysm.
 * Copyright (C) 2022 Matthew Cain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
 * This deserializer is used to deserialize {@link TileConfigJsonObject} from JSON.
 */
public class TileConfigJsonDeserializer extends JsonObjectDeserializer<TileConfigJsonObject> {

	TileConfigJsonDeserializer() {
		super(TileConfigJsonObject.class);
	}

	@Override
	void deserializeObjectMembers(Gson gson, JsonObject object, TileConfigJsonObject target, String entry) {

		JsonElement element = object.get(entry);
		if (element == null) {
			return;
		}
		if (entry.equals("tile_info")) {
			Optional<List<TileInfoJsonObject>> jsonObjects = JsonObjectBuilder.<TileInfoJsonObject>create()
					.ofType(TileInfoJsonObject.class)
					.withListTypeToken(new TypeToken<>() {})
					.buildAsList(element.toString());

			Field field = Objects.requireNonNull(jsonObjectFields.get("tile_info"));
			if (jsonObjects.isEmpty()) {
				throw new NullJsonObjectException("tile_info", TileInfoJsonObject.class);
			}
			changeFieldValue(field, target, jsonObjects.get().get(0));
		}
		else if (entry.equals("tiles-new")) {
			Optional<List<TileAtlasJsonObject>> jsonObjects = JsonObjectBuilder.<TileAtlasJsonObject>create()
					.ofType(TileAtlasJsonObject.class)
					.withListTypeToken(new TypeToken<>() {})
					.withDeserializer(TileAtlasJsonDeserializer.class)
					.buildAsList(element.toString());

			Field field = Objects.requireNonNull(jsonObjectFields.get("tiles-new"));
			if (jsonObjects.isEmpty()) {
				throw new NullJsonObjectException("tiles-new", TileAtlasJsonObject.class);
			}
			changeFieldValue(field, target, jsonObjects.get());
		}
	}
}
