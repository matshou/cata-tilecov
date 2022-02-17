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
package io.matshou.cata.tilecov;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import io.matshou.cata.tilecov.config.Config;
import io.matshou.cata.tilecov.coverage.TilesetCoverage;
import io.matshou.cata.tilecov.coverage.TilesetCoverageReport;
import io.matshou.cata.tilecov.json.CataJsonObject;
import io.matshou.cata.tilecov.tile.CataJsonFileTree;

public class Main {

	static final Map<Argument, Object> APP_ARGS = new HashMap<>();
	private static final Splitter SPLITTER = Splitter.on("=");

	/**
	 * Represents available application arguments.
	 */
	enum Argument {
		GAME_DIRECTORY("gameDir", "GAME_DIR", true) {
			@Override
			Object getAsObject(String value) {
				return Paths.get(!value.isEmpty() ? value : ".");
			}

			@Override
			void validate(String value) {
				// game directory has to exist and be an actual directory
				File gameDirectory = ((Path) getAsObject(value)).toFile();
				if (!gameDirectory.exists()) {
					throw new IllegalArgumentException("Game directory does not exist: " + value);
				}
				if (!gameDirectory.isDirectory()) {
					throw new IllegalArgumentException("Game directory is not a valid directory: " + value);
				}
			}
		},
		OUTPUT_DIR("outputDir", "OUTPUT_DIR", true) {
			@Override
			Object getAsObject(String value) {
				return Paths.get(!value.isEmpty() ? value : "reports");
			}

			@Override
			void validate(String value) {
				// output directory path has to NOT point to an existing file
				Path outputDir = ((Path) getAsObject(value));
				if (java.nio.file.Files.isRegularFile(outputDir)) {
					throw new IllegalArgumentException("Output directory needs to be a directory: " + value);
				}
			}
		};
		final String appArgName, sysPropName;
		private final boolean optional;

		/**
		 * Converts the given string to an {@code Object} intended by argument.
		 *
		 * @param value string to convert.
		 * @return given argument value as {@code Object}.
		 */
		abstract Object getAsObject(String value);

		/**
		 * Validate the given argument value for this argument.
		 *
		 * @param value argument value to validate.
		 *
		 * @throws IllegalArgumentException if value fails validation.
		 */
		abstract void validate(String value) throws IllegalArgumentException;

		Argument(String appArgName, String sysPropName, boolean optional) {
			this.appArgName = appArgName;
			this.sysPropName = sysPropName;
			this.optional = optional;
		}
	}

	/**
	 * Cata-TileCov application entry point.
	 * <p>
	 * This is the first class that gets called with you run the application.
	 *
	 * @param args array of application arguments.
	 * @see Main.Argument
	 */
	public static void main(String[] args) throws IOException {

		// initialize configuration file
		Config.initialize();

		// parse and validate app arguments
		handleAppArgs(args);

		Path gameDir = getGameDirectory();
		Path gameJsonDir = gameDir.resolve("data/json");
		if (!gameJsonDir.toFile().exists()) {
			throw new FileNotFoundException("Unable to find 'data/json' in game root directory: " + gameDir);
		}
		Path gfxDir = gameDir.resolve("gfx");
		if (!gfxDir.toFile().exists()) {
			throw new FileNotFoundException("Unable to find 'gfx' directory in: " + gameDir);
		}
		String[] jsonDirectories = new String[]{
				"items", "monsters"
		};
		Set<CataJsonFileTree> cataJsonFileTrees = new HashSet<>();
		for (String sJsonDir : jsonDirectories) {
			Path jsonDir = Paths.get(sJsonDir);
			cataJsonFileTrees.add(new CataJsonFileTree(gameJsonDir, jsonDir));
		}
		Set<TilesetCoverage> tilesetCoverages = new HashSet<>();
		for (Path path : Files.find(gfxDir, 1, (p, bfa) ->
				p != gfxDir && bfa.isDirectory()).collect(java.util.stream.Collectors.toSet())) {

			TilesetCoverage.Builder builder = TilesetCoverage.Builder.create(path).excludeOverlays();
			for (CataJsonFileTree fileTree : cataJsonFileTrees) {
				for (Map.Entry<Path, ImmutableSet<CataJsonObject>> entry : fileTree.entrySet()) {
					builder.withCataJsonObjects(gameJsonDir.resolve(entry.getKey()), entry.getValue());
				}
			}
			tilesetCoverages.add(builder.build());
		}
		TilesetCoverageReport coverageReport = new TilesetCoverageReport(tilesetCoverages);
		coverageReport.writeToFile(((File) APP_ARGS.get(Argument.OUTPUT_DIR)).toPath());
	}

	/**
	 * Parse, validate and store given application arguments.
	 *
	 * @param args array of arguments to handle.
	 *
	 * @throws IllegalArgumentException when any application argument is malformed.
	 * @throws IllegalStateException when missing non-optional application argument.
	 */
	static void handleAppArgs(String[] args) {

		// parse application arguments and create a map of properties
		Map<String, String> appArgs = new HashMap<>();
		for (String argEntry : args) {
			argEntry = argEntry.trim();
			if (argEntry.isEmpty()) {
				continue;
			}
			List<String> argProperty = SPLITTER.splitToList(argEntry);
			// always expect an array of properties separated by '=' delimiter
			if (argProperty.size() != 2) {
				String msg = "Malformed application arguments: ";
				throw new IllegalArgumentException(msg + String.join(",", args));
			}
			appArgs.put(argProperty.get(0), argProperty.get(1));
		}
		for (Argument property : Argument.values()) {
			String appArgValue = appArgs.get(property.appArgName);
			String systemProp = System.getProperty(property.sysPropName);
			// system properties always override application properties
			if (systemProp != null) {
				appArgValue = systemProp;
			}
			else if (appArgValue == null) {
				appArgValue = "";
			}
			if (!appArgValue.isEmpty()) {
				// validate argument values before storing them to map
				property.validate(appArgValue);
				// store argument value as object in map
				APP_ARGS.put(property, property.getAsObject(appArgValue));
				continue;
			}
			else if (!property.optional) {
				String msg = "Missing non-optional application argument: %s(%s)";
				throw new IllegalStateException(String.format(msg, property.appArgName, property.sysPropName));
			}
			Config.Entry configEntry = Config.Entry.values()[property.ordinal()];
			APP_ARGS.put(property, Config.getProperty(configEntry.name, Object.class));
		}
	}

	/**
	 * @return path to Cataclysm game directory.
	 */
	public static Path getGameDirectory() {
		return (Path) APP_ARGS.get(Argument.GAME_DIRECTORY);
	}
}
