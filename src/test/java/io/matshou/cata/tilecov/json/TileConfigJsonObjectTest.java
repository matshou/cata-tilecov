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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("SpellCheckingInspection")
public class TileConfigJsonObjectTest {

	private TileConfigJsonObject tileConfig;

	@BeforeEach
	void readTileConfigFromJson() throws IOException {

		Optional<TileConfigJsonObject> oTileConfig = JsonObjectBuilder.<TileConfigJsonObject>create()
				.ofType(TileConfigJsonObject.class)
				.withTypeToken(new TypeToken<>() {})
				.withDeserializer(TileConfigJsonDeserializer.class)
				.build(Paths.get("tile_config.json"));

		Assertions.assertTrue(oTileConfig.isPresent());
		tileConfig = oTileConfig.get();
	}

	@Test
	void shouldDeserializeTileInfoFromJson() {

		Optional<TileInfoJsonObject> oTileInfo = tileConfig.getTileInfo();
		Assertions.assertTrue(oTileInfo.isPresent());
		TileInfoJsonObject tileInfo = oTileInfo.get();

		Assertions.assertEquals(2, tileInfo.getPixelScale());
		Assertions.assertEquals(32, tileInfo.getWidth());
		Assertions.assertEquals(32, tileInfo.getHeight());
	}

	@Test
	void shouldDeserializeTileAtlasesFromJson() {

		List<TileAtlasJsonObject> tileAtlases = tileConfig.getTileAtlases();
		Assertions.assertEquals(2, tileAtlases.size());

		TileAtlasJsonObject tileAtlas = tileAtlases.get(0);
		TileAtlasJsonObject moreTileAtlas = tileAtlases.get(1);

		Assertions.assertEquals("tiles.png", tileAtlas.getFilename());
		Assertions.assertEquals("moretiles.png", moreTileAtlas.getFilename());

		// tiles.png
		List<String> overlayTiles = ImmutableList.of(
				"overlay_mutation_GOURMAND",
				"overlay_mutation_male_GOURMAND",
				"overlay_mutation_active_GOURMAND"
		);
		List<List<String>> expectedTiles = List.of(
				List.of("10mm"),
				List.of("t_wall"),
				List.of("vp_atomic_lamp"),
				List.of("t_dirt"),
				overlayTiles
		);
		List<TilesJsonObject> tiles = tileAtlas.getTiles();
		Assertions.assertEquals(expectedTiles.size(), tiles.size());

		for (int i = 0; i < expectedTiles.size(); ++i) {
			List<String> expected = new ArrayList<>(expectedTiles.get(i));
			Assertions.assertFalse(expected.retainAll(tiles.get(i).getTileId()));
		}

		// moretiles.png
		expectedTiles = List.of(List.of("xxx", "yyy"));

		List<TilesJsonObject> moreTiles = moreTileAtlas.getTiles();
		Assertions.assertEquals(expectedTiles.size(), moreTiles.size());

		for (int i = 0; i < expectedTiles.size(); ++i) {
			List<String> expected = new ArrayList<>(expectedTiles.get(i));
			Assertions.assertFalse(expected.retainAll(moreTiles.get(i).getTileId()));
		}
	}
}
