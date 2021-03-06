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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
		if (element != null) {
			deserializeObjectProperty(element, target, entry);
		}
	}
}
