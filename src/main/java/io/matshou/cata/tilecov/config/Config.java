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
package io.matshou.cata.tilecov.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharSink;
import com.google.common.io.Files;

/**
 * This class represents application configuration file.
 * <p>
 * Initialize the configuration file by calling {@link #initialize()}.
 */
public class Config {

	// configuration filename
	static final String FILENAME = "tilecov.ini";

	// map of configuration properties
	static ImmutableMap<String, Object> properties;

	public enum Entry {

		/**
		 * Path to Cataclysm game directory.
		 * <p>
		 * This property has to point to an existing directory.
		 */
		GAME_DIR("GAME_DIR", ".", p ->
		{
			File gameDir = Paths.get(p).toFile();
			if (!gameDir.exists()) {
				String message = "directory not found (%s)";
				throw new IllegalConfigPropertyException("GAME_DIR", String.format(message, p));
			}
			if (!gameDir.isDirectory()) {
				String message = "path is not a directory (%s)";
				throw new IllegalConfigPropertyException("GAME_DIR", String.format(message, p));
			}
			return gameDir;
		}, "Path to Cataclysm game directory", false),
		/**
		 * Path to coverage report output directory.
		 * <p>
		 * This property has to point to a non-existing file or an existing directory.
		 */
		OUTPUT_DIR("OUTPUT_DIR", "reports", p ->
		{
			Path outputDir = Paths.get(p);
			if (java.nio.file.Files.isRegularFile(outputDir)) {
				String message = "path is not a directory (%s)";
				throw new IllegalConfigPropertyException("OUTPUT_DIR", String.format(message, p));
			}
			return outputDir.toFile();
		}, "Path to coverage report output directory", false);

		public final String name;
		final String defaultValue;
		final String comment;
		final boolean optional;
		final Function<String, ?> type;

		Entry(String name, String defaultValue, Function<String, ?> type, String comment, boolean optional) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.comment = comment;
			this.optional = optional;
			this.type = type;
		}
	}

	/**
	 * Perform basic validation on given properties.
	 * <p>
	 * Only properties that match {@link Entry} name values will be validated.
	 * Other property entries will be ignored.
	 *
	 * @param properties properties to validate.
	 *
	 * @throws MissingConfigPropertyException when any property entry is missing.
	 * @throws IllegalConfigPropertyException when any non-optional property is empty.
	 */
	@Contract(pure = true)
	private static void validate(Properties properties) throws ConfigPropertyException {

		for (Entry entry : Entry.values()) {
			// non-optional properties should be defined
			if (!entry.optional) {
				Object obj = properties.get(entry.name);
				if (obj == null) {
					throw new MissingConfigPropertyException(entry.name);
				}
				else if (((String) obj).isEmpty()) {
					throw new IllegalConfigPropertyException(entry, "property is not optional");
				}
			}
		}
	}

	/**
	 * Initialize the configuration file in a directory with given path.
	 *
	 * @param configDir path to directory where the config file resides.
	 *
	 * @throws IOException when there was an error creating config file or loading properties.
	 * @throws ConfigPropertyException when any property has failed validation.
	 * @see #initialize()
	 */
	static void initialize(Path configDir) throws IOException, ConfigPropertyException {

		File configFile = configDir.resolve(FILENAME).toFile();
		if (!configFile.exists()) {
			if (!configFile.createNewFile()) {
				throw new IOException("Unable to create config file: " + configFile.getPath());
			}
			StringBuilder sb = new StringBuilder();
			CharSink sink = Files.asCharSink(configFile, Charset.defaultCharset());
			for (Entry entry : Entry.values()) {
				if (!entry.comment.isEmpty()) {
					sb.append("# ").append(entry.comment).append('\n');
				}
				sb.append(entry.name).append('=').append(entry.defaultValue).append('\n');
			}
			sink.write(sb.toString());
		}
		// load configuration file from the path
		final Properties propertiesFromFile = new Properties();
		try (FileReader stream = new FileReader(configFile)) {
			propertiesFromFile.load(stream);
		}
		// validate all property entries
		validate(propertiesFromFile);

		// Convert and store all entries to intended class object types
		// the entries will be stored as proper types in an immutable map
		Map<String, Object> tmpProperties = new HashMap<>();
		for (Entry entry : Entry.values()) {
			String propertyValue = (String) propertiesFromFile.get(entry.name);
			tmpProperties.put(entry.name, entry.type.apply(propertyValue));
		}
		properties = ImmutableMap.copyOf(tmpProperties);
	}

	/**
	 * Initialize the configuration file in application root directory.
	 * <p>
	 * If the configuration file in the given directory does not exist
	 * the config property file will be created with default property values.
	 * <p>
	 * All property entries that match {@link Entry} name values will be validated,
	 * converted to their intended class object types and stored in immutable map.
	 * Other property entries will be ignored.
	 *
	 * @throws IOException when there was an error creating config file or loading properties.
	 * @throws ConfigPropertyException when any property has failed validation.
	 */
	public static void initialize() throws IOException {
		initialize(Paths.get("."));
	}

	/**
	 * Find and return the property associated with given name.
	 *
	 * @param name Name of the property to get.
	 * @param type Class of expected property type.
	 * @param <T> expected property type.
	 * @return the found property cast to {@code T} or {@code null} if no property was found.
	 *
	 * @throws ClassCastException if the found property is not assignable to {@code T}.
	 */
	@Contract(pure = true)
	public static @Nullable <T> T getProperty(String name, Class<T> type) {

		Object property = properties.get(name);
		return property != null ? type.cast(property) : null;
	}
}
