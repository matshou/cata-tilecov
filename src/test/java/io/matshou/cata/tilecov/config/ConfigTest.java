package io.matshou.cata.tilecov.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

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
    void shouldThrowExceptionWhenConfigFileMissing(@TempDir Path tempDir) {
        assertThrows(FileNotFoundException.class, () -> Config.initialize(tempDir));
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
