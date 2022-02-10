package io.matshou.cata.tilecov.tile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.reflect.TypeToken;

import io.matshou.cata.tilecov.json.CataJsonDeserializer;
import io.matshou.cata.tilecov.json.CataJsonObject;
import io.matshou.cata.tilecov.json.JsonObjectBuilder;
import io.matshou.cata.tilecov.json.NullJsonObjectException;

/**
 * This class represents a file-tree of JSON files in Cataclysm directory.
 * <p>
 * The tree is created and populated on class construction.
 */
public class CataJsonFileTree {

	private final ImmutableMap<Path, ImmutableSet<CataJsonObject>> fileTreeMap;

	private static boolean shouldIncludeInTree(Path path, BasicFileAttributes attributes, Set<Path> whitelist) {

		// only json files should be included
		if (!attributes.isRegularFile() || !path.toString().endsWith(".json")) {
			return false;
		}
		Path parent = path.getParent();
		// path has to have a parent directory path that is whitelisted
		return whitelist.isEmpty() || (parent != null && whitelist.contains(parent));
	}

	/**
	 * Construct and populate a JSON file-tree for given directory path.
	 *
	 * @param path path to directory as starting point for mapping file tree.
	 * @param whitelist {@code Set} of directories to consider including in file tree.
	 * When whitelist is specified all paths that do not start with any whitelisted directory
	 * path will be excluded from the file tree.
	 *
	 * @throws IOException when an I/O exception occurs while walking files or building JSON object.
	 * @throws FileNotFoundException when given path does not point to an existing file.
	 * @throws IllegalArgumentException when given path does not represent a valid directory.
	 * @throws NullJsonObjectException when building a JSON object returns {@code null}.
	 */
	public CataJsonFileTree(Path path, Set<Path> whitelist) throws IOException, NullJsonObjectException {

		File fileTreeDir = path.toFile();
		if (!fileTreeDir.exists()) {
			throw new FileNotFoundException("Unable to find JSON directory: " + path);
		}
		if (!fileTreeDir.isDirectory()) {
			throw new IllegalArgumentException("Expected path to be directory: " + path);
		}
		Map<Path, ImmutableSet<CataJsonObject>> tempFileTree = new HashMap<>();
		for (Path jsonFile : Files.find(path, 10, (p, bfa) ->
				shouldIncludeInTree(path.relativize(p), bfa, whitelist)).collect(Collectors.toSet())) {

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
			Path relativePath = path.relativize(jsonFile);

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
		this(path, Set.of());
	}

	/**
	 * Get {@code CataJsonObject}'s registered under given directory path.
	 *
	 * @param path path to directory to get the files for.
	 * @return immutable list of objects representing JSON files in given directory
	 * or an empty {@code Set} if no JSON objects are mapped to given path.
	 */
	public ImmutableSet<CataJsonObject> getJsonObjects(Path path) {

		Set<CataJsonObject> result = new HashSet<>();
		for (Map.Entry<Path, ImmutableSet<CataJsonObject>> entry : fileTreeMap.entrySet()) {
			if (entry.getKey().startsWith(path)) {
				result.addAll(entry.getValue());
			}
		}
		return ImmutableSet.copyOf(result);
	}
}
