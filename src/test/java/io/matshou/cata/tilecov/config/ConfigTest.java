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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharSink;
import com.google.common.io.Files;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

	private static File createConfigFile(Path dir) throws IOException {

		File configFile = dir.resolve(Config.FILENAME).toFile();

		// create configuration file
		assertFalse(configFile.exists());
		assertTrue(configFile.createNewFile());

		return configFile;
	}

	@Test
	void shouldCreateConfigFileWhenMissing(@TempDir Path tempDir) throws IOException {

		assertDoesNotThrow(() -> Config.initialize(tempDir));
		File configFile = tempDir.resolve(Config.FILENAME).toFile();
		assertTrue(configFile.exists());

		java.util.List<String> expected = new ArrayList<>();
		for (Config.Entry entry : Config.Entry.values()) {
			if (!entry.comment.isEmpty()) {
				expected.add("# " + entry.comment);
			}
			expected.add(entry.name + '=' + entry.defaultValue);
		}
		java.util.List<String> actual = Files.readLines(configFile, Charset.defaultCharset());
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < actual.size(); ++i) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}

	@Test
	void shouldThrowExceptionWhenOptionalPropertyMissing(@TempDir Path tempDir) throws IOException {

		createConfigFile(tempDir);
		assertThrows(MissingConfigPropertyException.class, () -> Config.initialize(tempDir));
	}

	@Test
	void shouldThrowExceptionWhenNonOptionalPropertyEmpty(@TempDir Path tempDir) throws IOException {

		File configFile = createConfigFile(tempDir);
		CharSink sink = Files.asCharSink(configFile, Charset.defaultCharset());

		for (Config.Entry entry : Config.Entry.values()) {
			if (!entry.optional) {
				sink.write(entry.name + '=');
			}
		}
		assertThrows(IllegalConfigPropertyException.class, () -> Config.initialize(tempDir));
	}

	@Test
	void shouldThrowExceptionWhenGameDirectoryNotExist(@TempDir Path tempDir) throws IOException {

		File configFile = createConfigFile(tempDir);
		CharSink sink = Files.asCharSink(configFile, Charset.defaultCharset());

		Path wrongPath = tempDir.resolve("pseudo/path");
		assertFalse(wrongPath.toFile().exists());

		sink.write(Config.Entry.GAME_DIR.name + '=' + wrongPath);
		assertThrows(IllegalConfigPropertyException.class, () -> Config.initialize(tempDir));
	}

	@Test
	void shouldThrowExceptionWhenGameDirectoryNotDirectory(@TempDir Path tempDir) throws IOException {

		File configFile = createConfigFile(tempDir);
		CharSink sink = Files.asCharSink(configFile, Charset.defaultCharset());

		Path notDirectory = tempDir.resolve(".file");
		assertTrue(notDirectory.toFile().createNewFile());

		sink.write(Config.Entry.GAME_DIR.name + '=' + notDirectory);
		assertThrows(IllegalConfigPropertyException.class, () -> Config.initialize(tempDir));
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	void shouldThrowExceptionWhenGettingPropertyWithWrongType() {

		Config.properties = ImmutableMap.of("entry", 0);
		assertThrows(ClassCastException.class, () -> Config.getProperty("entry", String.class));
		assertDoesNotThrow(() -> Config.getProperty("entry", Integer.class));
	}
}
