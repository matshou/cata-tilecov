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

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Contract;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.matshou.cata.tilecov.json.CataIdentifiableFilter;
import io.matshou.cata.tilecov.json.CataJsonObject;
import io.matshou.cata.tilecov.tile.CataTileset;

/**
 * This class contains data on {@link CataTileset} coverage.
 */
public class TilesetCoverage {

	/**
	 * This class represents the type or quality of object tile coverage.
	 */
	public enum CoverageType {

		/**
		 * This coverage quality indicates that an object uses a unique tile.
		 */
		UNIQUE,

		/**
		 * This coverage quality indicates that an object does not use a unique tile
		 * and is instead inheriting a tile from another object either through
		 * {@code looks_like} or {@code copy-from} json properties.
		 */
		INHERITED,

		/**
		 * This coverage quality indicates that an object does not use
		 * a unique tile and is not inheriting a tile from another object.
		 */
		NO_COVERAGE
	}

	/**
	 * This map has {@code Path} keys mapped to coverage data containing
	 * id entries mapped to coverage quality types. The path entries
	 * represent JSON files the coverage data is derived from.
	 */
	final ImmutableMap<Path, ImmutableMap<String, CoverageType>> data;
	private final CataTileset tileset;

	private TilesetCoverage(CataTileset tileset, Map<Path,
			Set<CataJsonObject>> jsonObjectsMapped, Set<CataIdentifiableFilter> filters) {

		this.tileset = tileset;
		Set<String> tileIds = tileset.getTileIds();
		Map<Path, ImmutableMap<String, CoverageType>> tempData = new HashMap<>();
		for (Map.Entry<Path, Set<CataJsonObject>> entry : jsonObjectsMapped.entrySet()) {
			Set<CataJsonObject> objects = entry.getValue();
			Map<String, CoverageType> fileCoverage = new HashMap<>();
			for (CataJsonObject object : objects) {
				// the object has been excluded by filters
				if (filters.stream().anyMatch(f -> f.match(object))) {
					continue;
				}
				String objectId = object.getIds().get(0);
				if (tileIds.contains(objectId)) {
					fileCoverage.put(objectId, CoverageType.UNIQUE);
					continue;
				}
				// copy object id from object that looks like the object
				CataJsonObject looksLike = object.looksLikeWhat(objects);
				if (!looksLike.equals(object)) {
					fileCoverage.put(objectId, CoverageType.INHERITED);
					continue;
				}
				fileCoverage.put(objectId, CoverageType.NO_COVERAGE);
			}
			tempData.put(entry.getKey(), ImmutableMap.copyOf(fileCoverage));
		}
		data = ImmutableMap.copyOf(tempData);
	}

	/**
	 * This builder is used to construct a {@link TilesetCoverage} object.
	 * <p>
	 * Call {@link #create(Path)} or {@link #create(CataTileset)} to create a new builder
	 * and {@link #build()} to instruct the builder to create a new {@code TilesetCoverage} instance.
	 */
	public static class Builder {

		private final CataTileset tileset;
		private final Set<CataIdentifiableFilter> idFilters = new HashSet<>();
		private final Map<Path, Set<CataJsonObject>> cataJsonObjects = new HashMap<>();

		private Builder(CataTileset tileset) {
			this.tileset = tileset;
		}

		private Builder(Path tilesetDir) throws IOException {
			this.tileset = new CataTileset(tilesetDir);
		}

		/**
		 * Create a new {@link Builder} instance for given path. Building with this configuration
		 * will create {@code TilesetCoverage} for tileset constructed from given path.
		 *
		 * @param tilesetDir directory to construct the tileset from.
		 * @return new instance of {@code Builder}.
		 *
		 * @throws IOException if an error occurred while constructing {@link CataTileset}.
		 */
		@Contract("_ -> new")
		public static Builder create(Path tilesetDir) throws IOException {
			return new Builder(tilesetDir);
		}

		/**
		 * Create a new {@link Builder} instance for given tileset.
		 *
		 * @param tileset tileset to create tileset coverage for.
		 * @return new instance of {@code Builder}.
		 */
		@Contract("_ -> new")
		public static Builder create(CataTileset tileset) {
			return new Builder(tileset);
		}

		/**
		 * Configure the builder to create {@code TilesetCoverage}
		 * with specified JSON objects that were parsed from given file path.
		 *
		 * @param path {@code Path} the specified objects should be mapped to.
		 * @param objects {@code Set} of objects to use to construct {@code TileCoverage}.
		 * @return instance of this builder.
		 */
		@Contract("_, _ -> this")
		public Builder withCataJsonObjects(Path path, Set<CataJsonObject> objects) {
			cataJsonObjects.put(path, objects);
			return this;
		}

		/**
		 * Configure the builder to exclude certain id's from coverage.
		 *
		 * @param emptyIds whether to exclude id's that are empty strings.
		 * @param overlays whether to exclude id's that represent overlays.
		 * @return instance of this builder.
		 */
		@Contract("_, _ -> this")
		public Builder exclude(boolean emptyIds, boolean overlays) {

			if (emptyIds) {
				idFilters.add(CataIdentifiableFilter.NO_EMPTY_ID);
			}
			else idFilters.remove(CataIdentifiableFilter.NO_EMPTY_ID);
			if (overlays) {
				idFilters.add(CataIdentifiableFilter.NO_OVERLAYS);
			}
			else idFilters.remove(CataIdentifiableFilter.NO_OVERLAYS);
			return this;
		}

		/**
		 * @return new instance of {@code TilesetCoverage} based on builder configuration.
		 */
		@Contract("-> new")
		public TilesetCoverage build() {
			return new TilesetCoverage(tileset, cataJsonObjects, idFilters);
		}
	}

	/**
	 * @return {@code CataTileset} associated with this coverage.
	 */
	public CataTileset getTileset() {
		return tileset;
	}

	/**
	 * Get immutable {@code Set} of id's that can be found for given path.
	 *
	 * @param path path to get the coverage data for.
	 * @return all id's that can be found in this {@code TileCoverage} for given path
	 * or an empty list if no coverage was found for given path.
	 */
	public ImmutableSet<String> getCoverage(Path path) {

		Map<String, CoverageType> coverageEntry = data.get(path);
		return coverageEntry != null ? ImmutableSet.copyOf(coverageEntry.keySet()) : ImmutableSet.of();
	}

	/**
	 * Get immutable {@code Set} of id's that are covered with given
	 * quality coverage that can be found for specified path.
	 *
	 * @param type coverage type to use as filter.
	 * @param path path to get the coverage of type data for.
	 * @return all id's that can be found in this {@code TileCoverage} of given type
	 * for given path or an empty list if no coverage was found for given path.
	 */
	public Set<String> getCoverageOfType(CoverageType type, Path path) {

		Set<String> result = new HashSet<>();
		Map<String, CoverageType> coverageMap = data.get(path);
		if (coverageMap != null) {
			coverageMap.entrySet().stream().filter(e ->
					e.getValue() == type).forEach(e -> result.add(e.getKey()));
		}
		return result;
	}
}