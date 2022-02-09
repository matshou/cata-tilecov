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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.reflect.TypeToken;

public class TileInfoJsonObjectTest {

	@Test
	void shouldDeserializeProperties() {

		String jsonString = JsonObjectTestHelper.createJsonString(Map.of(
				"pixelscale", "2",
				"width", "32",
				"height", "32",
				"iso", "true"
		));
		Optional<List<TileInfoJsonObject>> oJsonObjects = JsonObjectBuilder.<TileInfoJsonObject>create()
				.ofType(TileInfoJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.buildAsList(jsonString);

		Assertions.assertTrue(oJsonObjects.isPresent());
		List<TileInfoJsonObject> jsonObjects = oJsonObjects.get();

		Assertions.assertEquals(1, jsonObjects.size());
		TileInfoJsonObject tileInfo = jsonObjects.get(0);

		Assertions.assertEquals(32, tileInfo.getHeight());
		Assertions.assertEquals(32, tileInfo.getWidth());
		Assertions.assertTrue(tileInfo.isIsometric());
		Assertions.assertEquals(2, tileInfo.getPixelScale());
	}

	@Test
	void shouldInitializeDefaultsWhenMissingProperties() {

		String jsonString = JsonObjectTestHelper.createJsonString(Map.of(
				"width", "32",
				"height", "32"
		));
		Optional<List<TileInfoJsonObject>> oJsonObjects = JsonObjectBuilder.<TileInfoJsonObject>create()
				.ofType(TileInfoJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.buildAsList(jsonString);

		Assertions.assertTrue(oJsonObjects.isPresent());
		List<TileInfoJsonObject> jsonObjects = oJsonObjects.get();

		Assertions.assertEquals(1, jsonObjects.size());
		TileInfoJsonObject tileInfo = jsonObjects.get(0);

		Assertions.assertEquals(32, tileInfo.getHeight());
		Assertions.assertEquals(32, tileInfo.getWidth());
		Assertions.assertFalse(tileInfo.isIsometric());
		Assertions.assertEquals(1, tileInfo.getPixelScale());
	}
}
