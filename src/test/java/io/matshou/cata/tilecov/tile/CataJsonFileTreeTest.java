package io.matshou.cata.tilecov.tile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import io.matshou.cata.tilecov.json.CataJsonObject;

public class CataJsonFileTreeTest {

	private static final String[] JSON_FILES_PATHS = {
			"data/json/furniture_and_terrain/furniture.json",
			"data/json/items/fluff.json",
			"data/json/monsters/slugs.json",
			"data/json/vehicles/vehicles.json"
	};
	private Path testTempDir;

	@BeforeEach
	void copyJsonFilesToTempDir(@TempDir Path tempDir) throws IOException {

		testTempDir = tempDir;
		for (String jsonFilePath : JSON_FILES_PATHS) {
			try (InputStream stream = CataJsonFileTreeTest.class.getResourceAsStream('/' + jsonFilePath)) {
				byte[] buffer = new byte[Objects.requireNonNull(stream).available()];
				Assertions.assertTrue(stream.read(buffer) > 0);

				File jsonDir = tempDir.resolve(jsonFilePath).toFile();
				File parentDir = jsonDir.getParentFile();
				if (!parentDir.exists()) {
					Assertions.assertTrue(parentDir.mkdirs());
				}
				Files.write(buffer, jsonDir);
				Assertions.assertTrue(jsonDir.exists());
			}
		}
	}

	@Test
	void shouldPopulateFileTreeWithJsonObjectsInDirectory() throws IOException {

		CataJsonFileTree fileTree = new CataJsonFileTree(testTempDir);
		for (String jsonFilesPath : JSON_FILES_PATHS) {
			Set<CataJsonObject> cataJsonObjects = fileTree.getJsonObjects(Paths.get(jsonFilesPath));
			Assertions.assertFalse(cataJsonObjects.isEmpty());
		}
	}

	@Test
	void shouldDeserializeJsonObjectsInFileTree() throws IOException {

		CataJsonFileTree fileTree = new CataJsonFileTree(testTempDir);
		Map<String, Set<String>> expectedMapped = ImmutableMap.of(
				JSON_FILES_PATHS[0], Set.of("", "f_floor_lamp", "f_floor_lamp_off", "f_floor_lamp_on"),
				JSON_FILES_PATHS[1], Set.of("magic_8_ball", "deck_of_cards", "coin_quarter", "family_photo"),
				JSON_FILES_PATHS[2], Set.of("mon_sludge_crawler", "mon_slug_giant"),
				JSON_FILES_PATHS[3], Set.of("custom", "none")
		);
		for (String filePath : JSON_FILES_PATHS) {
			ImmutableSet<CataJsonObject> jsonObjects = fileTree.getJsonObjects(Paths.get(filePath));
			Assertions.assertFalse(jsonObjects.isEmpty());

			Set<String> expected = expectedMapped.get(filePath);
			Assertions.assertNotNull(expected
			);
			Set<String> actual = new HashSet<>();
			jsonObjects.forEach(o -> actual.add(o.getId()));
			Assertions.assertFalse(actual.retainAll(expected));
		}
	}

	@Test
	void shouldRespectWhitelistWhenBuildingFileTree() throws IOException {

		Set<Path> whitelistDirectories = Set.of(
				Paths.get("data/json/items/"),
				Paths.get("data/json/monsters/")
		);
		CataJsonFileTree fileTree = new CataJsonFileTree(testTempDir, whitelistDirectories);
		Set<Path> expectedPaths = Set.of(
				Paths.get("data/json/items/fluff.json"),
				Paths.get("data/json/monsters/slugs.json")
		);
		for (Path expectedPath : expectedPaths) {
			Assertions.assertFalse(fileTree.getJsonObjects(expectedPath).isEmpty());
		}
		Set<Path> unexpectedPaths = Set.of(
				Paths.get("data/json/furniture_and_terrain/furniture.json"),
				Paths.get("data/json/vehicles/vehicles.json")
		);
		for (Path unexpectedPath : unexpectedPaths) {
			Assertions.assertTrue(fileTree.getJsonObjects(unexpectedPath).isEmpty());
		}
	}
}
