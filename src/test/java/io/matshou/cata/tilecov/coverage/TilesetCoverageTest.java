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
package io.matshou.cata.tilecov.coverage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.reflect.TypeToken;

import io.matshou.cata.tilecov.UnitTestResources;
import io.matshou.cata.tilecov.json.CataJsonDeserializer;
import io.matshou.cata.tilecov.json.CataJsonObject;
import io.matshou.cata.tilecov.json.JsonObjectBuilder;
import io.matshou.cata.tilecov.tile.CataTileset;

import static io.matshou.cata.tilecov.coverage.TilesetCoverage.CoverageType;

public class TilesetCoverageTest extends UnitTestResources {

	private final Set<CataJsonObject> jsonItemObjects = new HashSet<>();
	private Path jsonPath, tilesetPath;

	@Override
	protected void setupUnitTest(File tempDir) throws IOException {
		super.setupUnitTest(tempDir);

		tilesetPath = getTempDir().resolve("gfx/purple_tileset");
		jsonPath = getTempDir().resolve("data/json/items/guns.json");
		Optional<List<CataJsonObject>> oJsonObjects = JsonObjectBuilder.<CataJsonObject>create()
				.ofType(CataJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.withDeserializer(CataJsonDeserializer.class)
				.buildAsList(jsonPath);

		Assertions.assertTrue(oJsonObjects.isPresent());
		jsonItemObjects.addAll(oJsonObjects.get());
	}

	@Test
	void shouldBuildTilesetCoverageFromPath() throws IOException {

		TilesetCoverage coverage = TilesetCoverage.Builder.create(tilesetPath)
				.withCataJsonObjects(jsonPath, jsonItemObjects).build();

		Set<String> expectedCoverage = Set.of(
				"90two", "cx4", "calico", "ar15", "sniper_rifle"
		);
		Set<String> actualCoverage = coverage.getCoverage(jsonPath);
		Assertions.assertFalse(new HashSet<>(expectedCoverage).retainAll(actualCoverage));
	}

	@Test
	void shouldBuildTilesetCoverageFromTilesetInstance() throws IOException {

		TilesetCoverage coverage = TilesetCoverage.Builder.create(new CataTileset(tilesetPath))
				.withCataJsonObjects(jsonPath, jsonItemObjects).build();

		Set<String> expectedCoverage = Set.of(
				"90two", "cx4", "calico", "ar15", "sniper_rifle"
		);
		Set<String> actualCoverage = coverage.getCoverage(jsonPath);
		Assertions.assertFalse(new HashSet<>(expectedCoverage).retainAll(actualCoverage));
	}

	@Test
	void shouldGenerateAccurateTilesetCoverage() throws IOException {

		TilesetCoverage coverage = TilesetCoverage.Builder.create(tilesetPath)
				.withCataJsonObjects(jsonPath, jsonItemObjects).build();

		Map<CoverageType, Set<String>> expectedCoverage = Map.of(
				CoverageType.UNIQUE, Set.of("calico", "ar15", "cx4"),
				CoverageType.INHERITED, Set.of("90two", "glock_19"),
				CoverageType.NO_COVERAGE, Set.of("sniper_rifle")
		);
		for (Map.Entry<CoverageType, Set<String>> entry : expectedCoverage.entrySet()) {
			Set<String> coverageForType = coverage.getCoverageOfType(entry.getKey(), jsonPath);
			Assertions.assertFalse(coverageForType.retainAll(entry.getValue()));
		}
	}

	@Test
	void shouldGenerateAccurateTilesetCoverageStats() throws IOException {

		TilesetCoverage coverage = TilesetCoverage.Builder.create(tilesetPath)
				.withCataJsonObjects(jsonPath, jsonItemObjects).build();

		TilesetCoverage.CoverageStats coverageStats = coverage.stats.get(jsonPath);
		Assertions.assertNotNull(coverageStats);

		Assertions.assertEquals(6, coverageStats.getObjectsTotal());
		Assertions.assertEquals(3, coverageStats.getUniqueCoverageTotal());
		Assertions.assertEquals(2, coverageStats.getInheritedTotal());
		Assertions.assertEquals(1, coverageStats.getNoCoverageTotal());
	}
}
