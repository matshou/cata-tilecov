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

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MainTest {

	private static final String GAME_DIRECTORY = Main.Argument.GAME_DIRECTORY.appArgName;

	private static Path assertDirectoryCreated(Path root, String path) {

		Path result = root.resolve(path);
		Assertions.assertTrue(result.toFile().mkdirs());
		return result;
	}

	@Test
	void shouldThrowExceptionWhenMissingNonOptionalArguments() {

		// app arguments are not supplied
		Assertions.assertThrows(IllegalStateException.class, () ->
				Main.handleAppArgs(new String[]{})
		);
		// app arguments are empty
		Assertions.assertThrows(IllegalStateException.class, () ->
				Main.handleAppArgs(new String[]{ "" })
		);
		// non-optional app argument is empty
		Assertions.assertThrows(IllegalStateException.class, () ->
				Main.handleAppArgs(new String[]{ GAME_DIRECTORY + '=' })
		);
	}

	@Test
	void shouldThrowExceptionWhenGivenMalformedApplicationArguments() {

		// app arguments have no delimiter
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				Main.handleAppArgs(new String[]{ "arg1 value1 arg2 value2" })
		);
		// app arguments have no value
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				Main.handleAppArgs(new String[]{ "arg1value1 arg2value2" })
		);
		// app arguments are using wrong delimiter
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				Main.handleAppArgs(new String[]{ "arg1-value1 arg2~value2" })
		);
		// app arguments are separated by comma
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				Main.handleAppArgs(new String[]{ "arg1=value1,arg2=value2" })
		);
		// app arguments are properly formatted
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				Main.handleAppArgs(new String[]{ "arg1=value1 arg2=value2" })
		);
	}

	@Test
	void shouldUseSystemPropertiesOverApplicationArguments(@TempDir Path tempDir) {

		Path systemCataDir = assertDirectoryCreated(tempDir, "data/games/cata");
		Path appCataDir = assertDirectoryCreated(tempDir, "data/cata");

		String GAME_DIRECTORY_SYS = Main.Argument.GAME_DIRECTORY.sysPropName;
		System.setProperty(GAME_DIRECTORY_SYS, systemCataDir.toString());

		Main.handleAppArgs(new String[]{ GAME_DIRECTORY + '=' + appCataDir });
		Assertions.assertEquals(systemCataDir, Main.getGameDirectory());

		// when property is not cleared other tests will fail
		System.clearProperty(GAME_DIRECTORY_SYS);
	}

	@Test
	void shouldParseApplicationArgumentAsObjects(@TempDir Path tempDir) {

		Path cataDirPath = assertDirectoryCreated(tempDir, "data/cata");
		Main.handleAppArgs(new String[]{ GAME_DIRECTORY + '=' + cataDirPath });
		Assertions.assertEquals(cataDirPath, Main.getGameDirectory());
	}

	@Test
	void shouldThrowExceptionWhenArgumentFailsValidation(@TempDir Path tempDir) throws IOException {

		Path dirPath = tempDir.resolve("pseudo/path");
		Assertions.assertFalse(dirPath.toFile().exists());

		// argument value is a non-existing directory
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				Main.handleAppArgs(new String[]{ GAME_DIRECTORY + "=" + tempDir.resolve("pseudo/path") })
		);
		Path filePath = tempDir.resolve(".file");
		Assertions.assertTrue(filePath.toFile().createNewFile());

		// argument value is not a valid directory
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				Main.handleAppArgs(new String[]{ GAME_DIRECTORY + "=" + filePath })
		);
	}
}
