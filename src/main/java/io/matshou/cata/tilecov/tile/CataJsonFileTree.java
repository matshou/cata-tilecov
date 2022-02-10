package io.matshou.cata.tilecov.tile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

	/**
	 * Construct and populate a JSON file-tree for given directory path.
	 *
	 * @param path path to directory as starting point for mapping file tree.
	 * @throws IOException when an I/O exception occurs while walking files or building JSON object.
	 * @throws FileNotFoundException when given path does not point to an existing file.
	 * @throws IllegalArgumentException when given path does not represent a valid directory.
	 * @throws NullJsonObjectException when building a JSON object returns {@code null}.
	 */
	public CataJsonFileTree(Path path) throws IOException, NullJsonObjectException {

		File fileTreeDir = path.toFile();
		if (!fileTreeDir.exists()) {
			throw new FileNotFoundException("Unable to find JSON directory: " + path);
		}
		if (!fileTreeDir.isDirectory()) {
			throw new IllegalArgumentException("Expected path to be directory: " + path);
		}
		Map<Path, ImmutableSet<CataJsonObject>> tempFileTree = new HashMap<>();
		for (Path jsonFile : Files.find(path, 10, (p, bfa) ->
				bfa.isRegularFile() && p.toString().endsWith(".json")).collect(Collectors.toSet())) {

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
	 * Get {@code CataJsonObject}'s registered for given path.
	 *
	 * @param path path to directory to get the files for.
	 * @return immutable list of objects representing JSON files in given directory
	 * or an empty {@code Set} if no JSON objects are mapped to given path.
	 */
	public ImmutableSet<CataJsonObject> getJsonObjects(Path path) {

		ImmutableSet<CataJsonObject> result = fileTreeMap.get(path);
		return result != null ? result : ImmutableSet.of();
	}
}
