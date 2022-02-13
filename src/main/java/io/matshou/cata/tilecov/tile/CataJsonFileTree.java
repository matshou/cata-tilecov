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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.reflect.TypeToken;

import io.matshou.cata.tilecov.json.*;

/**
 * This class represents a file-tree of all JSON files in a Cataclysm file directory.
 * <p>
 * The tree is created and populated on class construction where files are searched with recursive
 * strategy using {@link Files#find(Path, int, BiPredicate, FileVisitOption...) Files.find(...)}
 * with an arbitrary maximum depth of 10 which limits the recursion to a depth of 10 directories.
 * <p>
 * Only non-directory files with {@code .json} file extension
 * are considered for inclusion. The files are further filtered using a pre-defined path blacklist,
 * which means that no paths are defined in the blacklist can be considered for inclusion.
 * The primary way to filter which files inside the root path get included is to call the constructor
 * with the {@code target} parameter set to directory path which you want to limit the inclusion to.
 */
public class CataJsonFileTree {

	private static final ImmutableSet<Path> PATH_BLACKLIST = ImmutableSet.of(
			Paths.get("monsters/monster_goals.json")
	);
	private final ImmutableMap<Path, ImmutableSet<CataJsonObject>> fileTreeMap;

	/**
	 * Returns whether the given path should be included in file tree.
	 *
	 * @param path path to consider for inclusion.
	 * @param attributes file attributes of given path.
	 * @param target directory to limit the inclusion to (can be {@code null}).
	 * @return {@code true} if the given path should be included in the file tree.
	 */
	private static boolean shouldInclude(Path path, BasicFileAttributes attributes, @Nullable Path target) {

		// only json files should be included
		if (!attributes.isRegularFile() || !path.toString().endsWith(".json")) {
			return false;
		}
		// check if path has been blacklisted
		if (PATH_BLACKLIST.stream().anyMatch(p -> p.compareTo(path) == 0)) {
			return false;
		}
		if (target != null) {
			Path parent = path.getParent();
			return parent != null && parent.compareTo(target) == 0;
		}
		return true;
	}

	/**
	 * Construct and populate a JSON file-tree for given directory path.
	 *
	 * @param rootPath path to directory as starting point for mapping file tree.
	 * @param targetPath path to directory that is the target of file tree mapping.
	 * When this is {@code null} all {@code .json} files will be included in the file tree.
	 * @throws IOException when an I/O exception occurs while walking files or building JSON object.
	 * @throws FileNotFoundException when given path does not point to an existing file.
	 * @throws IllegalArgumentException when given path does not represent a valid directory.
	 * @throws NullJsonObjectException when building a JSON object returns {@code null}.
	 */
	public CataJsonFileTree(Path rootPath, @Nullable Path targetPath) throws IOException, NullJsonObjectException {

		File fileTreeDir = rootPath.toFile();
		if (!fileTreeDir.exists()) {
			throw new FileNotFoundException("Unable to find JSON directory: " + rootPath);
		}
		if (!fileTreeDir.isDirectory()) {
			throw new IllegalArgumentException("Expected path to be directory: " + rootPath);
		}
		Map<Path, ImmutableSet<CataJsonObject>> tempFileTree = new HashMap<>();
		for (Path jsonFile : Files.find(rootPath, 10, (p, bfa) ->
				shouldInclude(rootPath.relativize(p), bfa, targetPath)).collect(Collectors.toSet())) {

			// deserialize the json file under found path
			Optional<List<CataJsonObject>> cataJsonObjects = JsonObjectBuilder.<CataJsonObject>create()
					.ofType(CataJsonObject.class)
					.withListTypeToken(new TypeToken<>() {})
					.withDeserializer(CataJsonDeserializer.class)
					.buildAsList(jsonFile);

			// create a relative path for json file
			// if path to file tree was /home/cata/data/json/
			// and path to file was /home/cata/data/json/monsters/slugs.json
			// then the relative path would be /monsters/slugs.json
			Path relativePath = rootPath.relativize(jsonFile);

			if (cataJsonObjects.isEmpty()) {
				throw new NullJsonObjectException(CataJsonObject.class);
			}
			tempFileTree.put(relativePath, ImmutableSet.copyOf(cataJsonObjects.get()));
		}
		// copy map over into an immutable map
		fileTreeMap = ImmutableMap.copyOf(tempFileTree);
	}

	/**
	 * Construct and populate a JSON file-tree for given directory path.
	 *
	 * @param path path to directory as starting point for mapping file tree.
	 *
	 * @throws IOException when an I/O exception occurs while walking files or building JSON object.
	 * @throws FileNotFoundException when given path does not point to an existing file.
	 * @throws IllegalArgumentException when given path does not represent a valid directory.
	 * @throws NullJsonObjectException when building a JSON object returns {@code null}.
	 */
	public CataJsonFileTree(Path path) throws IOException {
		this(path, null);
	}

	/**
	 * Get {@link CataJsonObject} instances registered under given directory path.
	 *
	 * @param path path to directory to get the files for.
	 * @param filters conditions under which objects should be filtered.
	 * @return immutable list of objects representing JSON files in given directory
	 * or an empty {@code Set} if no JSON objects are mapped to given path.
	 */
	public Set<CataJsonObject> getJsonObjects(Path path, CataIdentifiableFilter... filters) {

		Set<CataJsonObject> result = new HashSet<>();
		for (Map.Entry<Path, ImmutableSet<CataJsonObject>> entry : fileTreeMap.entrySet()) {
			if (entry.getKey().startsWith(path)) {
				result.addAll(entry.getValue().stream().filter(v ->
						Arrays.stream(filters).noneMatch(f -> f.match(v))).collect(Collectors.toSet()));
			}
		}
		return result;
	}

	/**
	 * @param filters conditions under which objects should be filtered.
	 * @return {@code Set} of id's that correspond to {@link CataJsonObject}
	 * * instances that are found in this file tree.
	 */
	public Set<String> getObjectIds(CataIdentifiableFilter... filters) {

		Set<CataJsonObject> filteredObjects = new HashSet<>();
		for (Map.Entry<Path, ImmutableSet<CataJsonObject>> entry : fileTreeMap.entrySet()) {
			filteredObjects.addAll(entry.getValue().stream().filter(v ->
					Arrays.stream(filters).noneMatch(f -> f.match(v))).collect(Collectors.toSet()));
		}
		Set<String> result = new HashSet<>();
		filteredObjects.forEach(o -> result.addAll(o.getIds()));
		return result;
	}
}
