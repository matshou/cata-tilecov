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
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.reflect.TypeToken;

import io.matshou.cata.tilecov.UnitTestResources;
import io.matshou.cata.tilecov.json.CataJsonDeserializer;
import io.matshou.cata.tilecov.json.CataJsonObject;
import io.matshou.cata.tilecov.json.JsonObjectBuilder;

public class TilesetCoverageReportTest extends UnitTestResources {

	private TilesetCoverage tilesetCoverage;

	@Override
	protected void setupUnitTest(File tempDir) throws IOException {
		super.setupUnitTest(tempDir);

		Path tilesetPath = getTempDir().resolve("gfx/diamond_tileset");
		Path[] jsonPaths = new Path[]{
				Paths.get("data/json/monsters/slugs.json"),
				Paths.get("data/json/items/fluff.json"),
				Paths.get("data/json/furniture_and_terrain/furniture.json")
		};
		TilesetCoverage.Builder builder = TilesetCoverage.Builder
				.create(tilesetPath).excludeOverlays();

		for (Path jsonPath : jsonPaths) {
			Optional<List<CataJsonObject>> oJsonObjects = JsonObjectBuilder.<CataJsonObject>create()
					.ofType(CataJsonObject.class)
					.withListTypeToken(new TypeToken<>() {})
					.withDeserializer(CataJsonDeserializer.class)
					.buildAsList(jsonPath);

			Assertions.assertTrue(oJsonObjects.isPresent());
			builder.withCataJsonObjects(jsonPath, new HashSet<>(oJsonObjects.get()));
		}
		tilesetCoverage = builder.build();
	}

	@Test
	void shouldGenerateTilesetCoverageReportToOutputDirectory(@TempDir Path tempDir) throws IOException {

		new TilesetCoverageReport(Set.of(tilesetCoverage)).writeToFile(tempDir);

		Assertions.assertTrue(tempDir.resolve("coverage.css").toFile().exists());
		Assertions.assertTrue(tempDir.resolve("coverage.html").toFile().exists());
	}
}
