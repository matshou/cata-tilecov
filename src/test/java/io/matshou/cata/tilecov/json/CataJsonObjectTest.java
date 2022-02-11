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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.gson.reflect.TypeToken;

public class CataJsonObjectTest {

	private static final Path FURNITURE = Paths.get("data/json/furniture_and_terrain/furniture.json");

	@Test
	void shouldDeserializeProperties() throws IOException {

		Optional<List<CataJsonObject>> oJsonObjects = JsonObjectBuilder.<CataJsonObject>create()
				.ofType(CataJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.withDeserializer(CataJsonDeserializer.class)
				.buildAsList(FURNITURE);

		Assertions.assertTrue(oJsonObjects.isPresent());
		List<CataJsonObject> jsonObjects = oJsonObjects.get();
		Assertions.assertEquals(4, jsonObjects.size());

		String[] expectedValues = {
				"furniture", "", "-", "-",
				"furniture", "f_floor_lamp", "floor lamp (no power)",
				"A tall standing lamp, plugged into a wall. This one has no power.",
				"furniture", "f_floor_lamp_off", "floor lamp (off)",
				"A tall standing lamp, meant to plug into a wall and light up a room.",
				"furniture", "f_floor_lamp_on", "floor lamp (on)",
				"A tall standing lamp, plugged into a wall."
		};
		Iterator<CataJsonObject> iter = jsonObjects.iterator();
		for (int i = 0; i < 16; i += 4) {
			CataJsonObject cataJsonObject = iter.next();

			String expectedType = expectedValues[i];
			Assertions.assertEquals(expectedType, cataJsonObject.getType());

			String expectedId = expectedValues[i + 1];
			Assertions.assertEquals('[' + expectedId + ']', cataJsonObject.getIds().toString());

			String expectedName = expectedValues[i + 2];
			Assertions.assertEquals(expectedName, cataJsonObject.getName());

			String expectedDescription = expectedValues[i + 3];
			Assertions.assertEquals(expectedDescription, cataJsonObject.getDescription());
		}
	}

	@Test
	void shouldDeserializeAnnotatedProperties() throws IOException {

		Optional<List<CataJsonObject>> oJsonObjects = JsonObjectBuilder.<CataJsonObject>create()
				.ofType(CataJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.withDeserializer(CataJsonDeserializer.class)
				.buildAsList(FURNITURE);

		Assertions.assertTrue(oJsonObjects.isPresent());
		List<CataJsonObject> jsonObjects = oJsonObjects.get();
		Assertions.assertEquals(4, jsonObjects.size());

		String[] expectedValues = {
				"", "",
				"", "f_floor_lamp_base",
				"f_floor_lamp", "f_floor_lamp_base",
				"f_floor_lamp", "f_floor_lamp_base"
		};
		Iterator<CataJsonObject> iter = jsonObjects.iterator();
		for (int i = 0; i < 8; i += 2) {
			CataJsonObject cataJsonObject = iter.next();

			String expectedLooksLike = expectedValues[i];
			Assertions.assertEquals(expectedLooksLike, cataJsonObject.looksLikeWhat());

			String expectedCopyFrom = expectedValues[i + 1];
			Assertions.assertEquals(expectedCopyFrom, cataJsonObject.copyFromWhat());
		}
	}

	@Test
	void shouldDeserializeJsonArrays() throws IOException {

		Optional<List<CataJsonObject>> oJsonObjects = JsonObjectBuilder.<CataJsonObject>create()
				.ofType(CataJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.withDeserializer(CataJsonDeserializer.class)
				.buildAsList(FURNITURE);

		Assertions.assertTrue(oJsonObjects.isPresent());
		List<CataJsonObject> jsonObjects = oJsonObjects.get();

		List<List<List<String>>> expected = ImmutableList.of(
				List.of(List.of("light_gray"), List.of()),
				List.of(List.of("blue"), List.of("white")),
				List.of(List.of("red"), List.of("purple", "orange", "green")),
				List.of(List.of("yellow"), List.of(""))
		);
		for (int i = 0; i < expected.size(); ++i) {
			CataJsonObject cataJsonObject = jsonObjects.get(i);
			List<List<String>> entry = expected.get(i);

			List<String> expectedColors = entry.get(0);
			List<String> expectedBgColors = entry.get(1);

			List<String> actualColors = cataJsonObject.getForegroundColor();
			Assertions.assertEquals(expectedColors.size(), actualColors.size());
			Assertions.assertFalse(new ArrayList<>(actualColors).retainAll(expectedColors));

			List<String> actualBgColors = cataJsonObject.getBackgroundColor();
			Assertions.assertEquals(expectedBgColors.size(), actualBgColors.size());
			Assertions.assertFalse(new ArrayList<>(actualBgColors).retainAll(expectedBgColors));
		}
	}

	@Test
	void shouldFailToDeserializeJsonArraysWithoutAdapter() throws IOException {

		Optional<List<CataJsonObject>> oJsonObjects = JsonObjectBuilder.<CataJsonObject>create()
				.ofType(CataJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.buildAsList(FURNITURE);

		Assertions.assertTrue(oJsonObjects.isPresent());
		List<CataJsonObject> jsonObjects = oJsonObjects.get();

		for (CataJsonObject cataJsonObject : jsonObjects) {
			Assertions.assertTrue(cataJsonObject.getForegroundColor().isEmpty());
			Assertions.assertTrue(cataJsonObject.getBackgroundColor().isEmpty());
		}
	}
}
