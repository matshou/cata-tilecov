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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import io.matshou.cata.tilecov.UnitTestResources;
import io.matshou.cata.tilecov.json.CataIdentifiableFilter;
import io.matshou.cata.tilecov.json.NullJsonObjectException;

@SuppressWarnings("CodeBlock2Expr")
public class TilesetTest extends UnitTestResources {

	private static final String[] TILESET_FILE_PATHS = {
			"gfx/sample_tileset/tile_config.json",
			"gfx/sample_tileset/tileset.txt",
			"gfx/red_tileset/tile_config.json",
			"gfx/red_tileset/tileset.txt",
			"gfx/blue_tileset/tile_config.json",
			"gfx/blue_tileset/tileset.txt"
	};

	@Test
	void shouldLoadTilesetMetadataFromPath() throws IOException {

		Map<Path, String> expectedNames = Map.of(
				Paths.get("gfx/sample_tileset"), "SampleTileset",
				Paths.get("gfx/red_tileset"), "RedTileset",
				Paths.get("gfx/blue_tileset"), "BlueTileset"
		);
		for (int i = 0; i < TILESET_FILE_PATHS.length; i += 2) {
			Path targetPath = getTempDir().resolve(TILESET_FILE_PATHS[i]).getParent();
			CataTileset tileset = new CataTileset(targetPath);

			Path relativePath = getTempDir().relativize(targetPath);
			Assertions.assertEquals(expectedNames.get(relativePath), tileset.getDisplayName());
			Assertions.assertEquals(targetPath.getFileName().toString(), tileset.getName());
		}
	}

	@Test
	void shouldThrowExceptionWhenTilesetDirOrMetadataNotFound() {

		// tileset directory not found
		Assertions.assertThrows(FileNotFoundException.class, () -> {
			new CataTileset(getTempDir().resolve("gfx/pseudo_tileset"));
		});
		Path testTilesetDir = getTempDir().resolve("test_tileset");
		Assertions.assertTrue(testTilesetDir.toFile().mkdir());

		// tileset metadata not found
		Assertions.assertThrows(FileNotFoundException.class, () -> {
			new CataTileset(testTilesetDir);
		});
	}

	@Test
	void shouldThrowExceptionWhenTilesetConfigPathNotDefinedInMetadata() throws IOException {

		Path testTilesetDir = getTempDir().resolve("test_tileset");
		Assertions.assertTrue(testTilesetDir.toFile().mkdir());

		File tilesetMetadata = testTilesetDir.resolve("tileset.txt").toFile();
		Assertions.assertTrue(tilesetMetadata.createNewFile());

		// tileset config path not defined
		Assertions.assertThrows(IllegalStateException.class, () -> {
			new CataTileset(testTilesetDir);
		});
		CharSink charSink = Files.asCharSink(
				tilesetMetadata, Charset.defaultCharset(), FileWriteMode.APPEND
		);
		charSink.write("JSON: tile_config.json");

		// tile_config.json file was not found
		Assertions.assertThrows(FileNotFoundException.class, () -> {
			new CataTileset(testTilesetDir);
		});
		File tileConfigFile = testTilesetDir.resolve("tile_config.json").toFile();
		Assertions.assertTrue(tileConfigFile.createNewFile());

		// tile config file produces a null JsonObject
		Assertions.assertThrows(NullJsonObjectException.class, () -> {
			new CataTileset(testTilesetDir);
		});
		charSink = Files.asCharSink(
				tileConfigFile, Charset.defaultCharset(), FileWriteMode.APPEND
		);
		charSink.write("{}");
		Assertions.assertDoesNotThrow(() -> new CataTileset(testTilesetDir));
	}

	@Test
	void shouldGetCompleteListOfTileIdsFromTileset() throws IOException {

		List<Set<String>> expectedTileIds = List.of(
				// sample_tileset
				Set.of("10mm", "t_wall", "vp_atomic_lamp", "t_dirt", "xxx", "yyy"),
				// red_tileset
				Set.of("mon_bear_cub", "mon_cat_sphynx", "vp_aisle_lights"),
				// blue_tileset
				Set.of("mag_electronics", "manual_electronics")
		);
		Iterator<Set<String>> iter = expectedTileIds.iterator();
		for (int i = 0; i < TILESET_FILE_PATHS.length; i += 2) {
			Path targetPath = getTempDir().resolve(TILESET_FILE_PATHS[i]).getParent();
			CataTileset tileset = new CataTileset(targetPath);

			Set<String> tileIds = tileset.getTileIds(
					CataIdentifiableFilter.NO_EMPTY_ID, CataIdentifiableFilter.NO_OVERLAYS
			);
			Assertions.assertEquals(iter.next(), tileIds);
		}
	}
}
