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
