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

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import io.matshou.cata.tilecov.json.*;

public class CataTileset {

	private final String name, displayName;
	private final TileConfigJsonObject tileConfig;

	/**
	 * Create a new {@code Tileset} instance for given directory path.
	 *
	 * @param path path to tileset directory.
	 *
	 * @throws IOException if an error occurred while loading properties from file.
	 * @throws FileNotFoundException if tileset directory or metadata file doesn't exist.
	 * @throws IllegalStateException if path to config file was not specified in metadata.
	 * @throws JsonSyntaxException if an error occurred while parsing tile config json.
	 */
	public CataTileset(Path path) throws IOException {

		File tilesetDir = path.toFile();
		if (!tilesetDir.exists()) {
			throw new FileNotFoundException("Tileset directory does not exist: " + path);
		}
		File tilesetTextFile = path.resolve("tileset.txt").toFile();
		if (!tilesetTextFile.exists()) {
			throw new FileNotFoundException("tileset.txt not found in directory: " + path);
		}
		Properties tilesetMetadata = new Properties();
		try (InputStream stream = new FileInputStream(tilesetTextFile)) {
			tilesetMetadata.load(stream);
		}
		name = tilesetMetadata.getProperty("NAME", "Unknown");
		displayName = tilesetMetadata.getProperty("VIEW", "Unknown");

		String tileConfigPath = tilesetMetadata.getProperty("JSON");
		if (tileConfigPath == null) {
			throw new IllegalStateException("Path to config file was not specified for tileset: " + name);
		}
		File tileConfigFile = new File(tilesetDir, tileConfigPath);
		if (!tileConfigFile.exists()) {
			throw new FileNotFoundException("Unable to find config file for tileset: " + name);
		}
		Optional<TileConfigJsonObject> oTileConfig = JsonObjectBuilder.<TileConfigJsonObject>create()
				.ofType(TileConfigJsonObject.class)
				.withTypeToken(new TypeToken<>() {})
				.withDeserializer(TileConfigJsonDeserializer.class)
				.build(tileConfigFile.toPath());

		if (oTileConfig.isEmpty()) {
			throw new NullJsonObjectException(TileConfigJsonObject.class);
		}
		tileConfig = oTileConfig.get();
	}

	/**
	 * @return name of this tileset as defined in metadata.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return display name for this tileset as defined in metadata.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param filters conditions under which tile id's should be filtered.
	 * @return {@code Set} of all object ID's that will be mapped to one or more tiles in this tileset.
	 */
	public Set<String> getTileIds(CataIdentifiableFilter... filters) {

		Set<TilesJsonObject> filteredTiles = new HashSet<>();
		for (TileAtlasJsonObject tileAtlas : tileConfig.getTileAtlases()) {
			filteredTiles.addAll(tileAtlas.getTiles().stream().filter(v ->
					Arrays.stream(filters).noneMatch(f -> f.match(v))).collect(Collectors.toSet()));
		}
		Set<String> result = new HashSet<>();
		filteredTiles.forEach(t -> result.addAll(t.getIds()));
		return result;
	}
}
