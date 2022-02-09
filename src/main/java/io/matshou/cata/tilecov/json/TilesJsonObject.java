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

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * This object contains individual tile information.
 *
 * @see <a href="https://github.com/cataclysmbnteam/Cataclysm-BN/blob/upload/doc/TILESET.md#tile_config">
 * Cataclysm: Bright Nights - Tileset: tile_config</a>
 */
@SuppressWarnings("unused")
public class TilesJsonObject {

	@SerializedArrayName("id")
	private @Nullable List<String> tileId;

	@SerializedArrayName("fg")
	private @Nullable List<String> tileFg;

	@SerializedArrayName("bg")
	private @Nullable List<String> tileBg;

	/**
	 * @return ID of the objects that will be mapped to this tile
	 * or an empty list if the property has not been defined.
	 */
	public ImmutableList<String> getTileId() {
		return tileId != null ? ImmutableList.copyOf(tileId) : ImmutableList.of();
	}

	/**
	 * @return indexes that point to position of sprite foregrounds in sprite atlas
	 * or an empty list if the property has not been defined.
	 */
	public ImmutableList<String> getForegroundIndex() {
		return tileFg != null ? ImmutableList.copyOf(tileFg) : ImmutableList.of();
	}

	/**
	 * @return indexes that point to position of sprite backgrounds in sprite atlas
	 * or an empty list if the property has not been defined.
	 */
	public ImmutableList<String> getBackgroundIndex() {
		return tileBg != null ? ImmutableList.copyOf(tileBg) : ImmutableList.of();
	}
}
