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
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * This class represents a tileset {@code tile_config.json} file.
 * <p>
 * Each legacy tileset has a {@code tile_config.json} describing how to map
 * the contents of a sprite sheet to various tile identifiers, different orientations, etc.
 */
@SuppressWarnings("unused")
public class TileConfigJsonObject {

	@SerializedObjectName("tile_info")
	private @Nullable TileInfoJsonObject tileInfo;

	@SerializedObjectName("tiles-new")
	private @Nullable List<TileAtlasJsonObject> tileAtlases;

	/**
	 * @return basic tileset information.
	 */
	public Optional<TileInfoJsonObject> getTileInfo() {
		return Optional.ofNullable(tileInfo);
	}

	/**
	 * @return list of tile atlases contained in this tileset
	 * or an empty list if the property is not defined.
	 */
	public ImmutableList<TileAtlasJsonObject> getTileAtlases() {
		return tileAtlases != null ? ImmutableList.copyOf(tileAtlases) : ImmutableList.of();
	}
}
