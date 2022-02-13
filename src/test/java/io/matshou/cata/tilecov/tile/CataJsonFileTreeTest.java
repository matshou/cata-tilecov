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
package io.matshou.cata.tilecov.tile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import io.matshou.cata.tilecov.UnitTestResources;
import io.matshou.cata.tilecov.json.CataIdentifiableFilter;
import io.matshou.cata.tilecov.json.CataJsonObject;

public class CataJsonFileTreeTest extends UnitTestResources {

	private static final String[] JSON_FILES_PATHS = {
			"furniture_and_terrain/furniture.json",
			"items/fluff.json",
			"monsters/slugs.json",
			"vehicles/vehicles.json"
	};
	private Path jsonDir;

	@Override
	protected void setupUnitTest(File tempDir) throws IOException {
		super.setupUnitTest(tempDir);
		jsonDir = getTempDir().resolve("data/json");
	}

	@Test
	void shouldPopulateFileTreeWithJsonObjectsInDirectory() throws IOException {

		CataJsonFileTree fileTree = new CataJsonFileTree(jsonDir);
		for (String jsonFilesPath : JSON_FILES_PATHS) {
			Set<CataJsonObject> cataJsonObjects = fileTree.getJsonObjects(Paths.get(jsonFilesPath));
			Assertions.assertFalse(cataJsonObjects.isEmpty());
		}
	}

	@Test
	void shouldDeserializeJsonObjectsInFileTree() throws IOException {

		CataJsonFileTree fileTree = new CataJsonFileTree(jsonDir);
		Map<String, Set<String>> expectedMapped = ImmutableMap.of(
				JSON_FILES_PATHS[0], Set.of("", "f_floor_lamp", "f_floor_lamp_off", "f_floor_lamp_on"),
				JSON_FILES_PATHS[1], Set.of("magic_8_ball", "deck_of_cards", "coin_quarter", "family_photo"),
				JSON_FILES_PATHS[2], Set.of("mon_sludge_crawler", "mon_slug_giant"),
				JSON_FILES_PATHS[3], Set.of("custom", "none")
		);
		for (String filePath : JSON_FILES_PATHS) {
			Set<CataJsonObject> jsonObjects = fileTree.getJsonObjects(Paths.get(filePath));
			Assertions.assertFalse(jsonObjects.isEmpty());

			Set<String> expected = expectedMapped.get(filePath);
			Assertions.assertNotNull(expected);

			Set<String> actual = new HashSet<>();
			jsonObjects.forEach(o -> actual.addAll(o.getIds()));
			Assertions.assertFalse(actual.retainAll(expected));
		}
	}

	@Test
	void shouldBuildFileTreeForTargetDirectory() throws IOException {

		Path[] unexpectedPaths = new Path[]{
				Paths.get("furniture_and_terrain/furniture.json"),
				Paths.get("monsters/slugs.json"),
				Paths.get("vehicles/vehicles.json")
		};
		Path expected = Paths.get("items");
		CataJsonFileTree fileTree = new CataJsonFileTree(jsonDir, expected);
		Assertions.assertFalse(fileTree.getJsonObjects(expected).isEmpty());

		for (Path unexpected : unexpectedPaths) {
			Assertions.assertTrue(fileTree.getJsonObjects(unexpected).isEmpty());
		}
	}

	@Test
	void shouldFilterOutObjectsThatHaveMissingId() throws IOException {

		Path expected = Paths.get("furniture_and_terrain/");
		CataJsonFileTree fileTree = new CataJsonFileTree(jsonDir, expected);
		Assertions.assertFalse(fileTree.getJsonObjects(expected).isEmpty());

		for (String objectId : fileTree.getObjectIds(CataIdentifiableFilter.NO_EMPTY_ID)) {
			Assertions.assertFalse(objectId.isEmpty());
		}
	}
}
