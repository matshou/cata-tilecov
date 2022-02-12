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
package io.matshou.cata.tilecov;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.io.Files;

import io.matshou.cata.tilecov.tile.CataJsonFileTreeTest;

/**
 * Test fixture class intended to be used by unit tests that need to read test resource files.
 * Classes that implement this class will have access to a temporary directory where
 * all test resource files will be copied to on each object instance construction.
 */
public abstract class UnitTestResources {

	private static final String[] RESOURCE_FILE_PATHS = {
			"data/json/furniture_and_terrain/furniture.json",
			"data/json/items/fluff.json",
			"data/json/items/guns.json",
			"data/json/monsters/slugs.json",
			"data/json/vehicles/vehicles.json",
			"gfx/sample_tileset/tile_config.json",
			"gfx/sample_tileset/tileset.txt",
			"gfx/red_tileset/tile_config.json",
			"gfx/red_tileset/tileset.txt",
			"gfx/blue_tileset/tile_config.json",
			"gfx/blue_tileset/tileset.txt",
			"gfx/purple_tileset/tile_config.json",
			"gfx/purple_tileset/tileset.txt"
	};
	private Path tempDir;

	protected void setupUnitTest(File tempDir) throws IOException {

		this.tempDir = tempDir.toPath();
		for (String filePath : RESOURCE_FILE_PATHS) {
			try (InputStream stream = CataJsonFileTreeTest.class.getResourceAsStream('/' + filePath)) {
				byte[] buffer = new byte[Objects.requireNonNull(stream).available()];
				Assertions.assertTrue(stream.read(buffer) > 0);

				File resourceFile = new File(tempDir, filePath);
				File parentDir = resourceFile.getParentFile();
				if (!parentDir.exists()) {
					Assertions.assertTrue(parentDir.mkdirs());
				}
				Files.write(buffer, resourceFile);
				Assertions.assertTrue(resourceFile.exists());
			}
		}
	}

	@BeforeEach
	void setupUnitTest(@TempDir Path tempDir) throws IOException {
		setupUnitTest(tempDir.toFile());
	}

	/**
	 * @return temporary directory for this test.
	 */
	protected Path getTempDir() {
		return tempDir;
	}
}
