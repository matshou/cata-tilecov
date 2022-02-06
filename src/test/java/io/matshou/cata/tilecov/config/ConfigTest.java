package io.matshou.cata.tilecov.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    private static File createConfigFile(Path dir) throws IOException {

        File configFile = dir.resolve(Config.FILENAME).toFile();

        // create configuration file
        assertFalse(configFile.exists());
        assertTrue(configFile.createNewFile());

        return configFile;
    }
}
