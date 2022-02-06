package io.matshou.cata.tilecov.config;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;

import com.google.common.collect.ImmutableMap;

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

    enum Entry {

        GAME_DIR("GAME_DIR", false, p -> {
            File gameDir = Paths.get(p).toFile();
            if (!gameDir.exists()) {
                throw new RuntimeException(String.format("Game directory not found: %s", p));
            }
            return gameDir;
        });

        final String name;
        final boolean optional;
        final Function<String, ?> type;

        Entry(String name, boolean optional, Function<String, ?> type) {
            this.name = name;
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
     * @throws ConfigPropertyException when property is either missing or malformed.
     */
    @Contract(pure = true)
    private static void validate(Properties properties) throws ConfigPropertyException {

        for(Entry entry : Entry.values()) {
            Object obj = properties.get(entry.name);
            if (obj == null) {
                throw new MissingConfigProperty(entry.name);
            }
            String value = (String) obj;
            if (!entry.optional && value.isEmpty()) {
                throw new IllegalConfigProperty(entry, "property is not optional");
            }
        }
    }

    /**
     * Initialize the configuration file.
     * <p>
     * All property entries that match {@link Entry} name values will be validated,
     * converted to their intended class object types and stored in immutable map.
     * Other property entries will be ignored.
     *
     * @throws IOException when config file was not found or there was an error loading Properties.
     * @throws ConfigPropertyException when any property has failed validation.
     */
    public static void initialize() throws IOException, ConfigPropertyException {

        Path configPath = Paths.get(FILENAME);
        if (!configPath.toFile().exists()) {
            String absolutePath = configPath.toAbsolutePath().toString();
            throw new FileNotFoundException("Unable to find config file: " + absolutePath);
        }
        // load configuration file from the path
        final Properties propertiesFromFile = new Properties();
        try (FileReader stream = new FileReader(FILENAME)) {
            propertiesFromFile.load(stream);
        }
        // validate all property entries
        validate(propertiesFromFile);

        // Convert and store all entries to intended class object types
        // the entries will be stored as proper types in an immutable map
        Map<String, Object> tmpProperties = new HashMap<>();
        for (Entry entry : Entry.values()) {
            tmpProperties.put(entry.name, entry.type.apply(entry.name));
        }
        properties = ImmutableMap.copyOf(tmpProperties);
    }

    /**
     * Find and return the property associated with given name.
     *
     * @param name Name of the property to get.
     * @param type Class of expected property type.
     * @param <T> expected property type.
     * @return the found property cast to {@code T}.
     * @throws ClassCastException if the found property is not assignable to {@code T}.
     */
    @Contract(pure = true)
    public static <T> T getProperty(String name, Class<T> type) {

        Object property = properties.get(name);
        return property != null ? type.cast(property) : null;
    }
}
