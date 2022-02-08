package io.matshou.cata.tilecov.json;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

/**
 * This class can build a class of type {@code T} that represents a JsonObject.
 * The building process can be configured to utilize a custom deserializer and a specific
 * {@link TypeToken} which will determine the type of object built by the builder.
 * <p>
 * <i>Note that the representing class does not need to actually extend {@code JsonObject} class.</i>
 *
 * @param <T> type of object that is expected to be the result of building with this builder.
 * In actuality the result is specified by {@link TypeToken} but it is also defined by this type.
 */
public class JsonObjectBuilder<T> {

    private @Nullable Class<T> jsonObjectType;
    private @Nullable Class<? extends JsonDeserializer<T>> deserializer;
    private @Nullable TypeToken<?> typeToken;

    @Contract("_ -> this")
    public JsonObjectBuilder<T> ofType(Class<T> type) {
        jsonObjectType = type;
        return this;
    }

    @Contract("_ -> this")
    public JsonObjectBuilder<T> withTypeToken(TypeToken<List<T>> token) {
        typeToken = token;
        return this;
    }

    @Contract("_ -> this")
    public JsonObjectBuilder<T> withDeserializer(Class<? extends JsonDeserializer<T>> clazz) {
        deserializer = clazz;
        return this;
    }

    @Contract("-> new")
    public static <T> JsonObjectBuilder<T> create() {
        return new JsonObjectBuilder<>();
    }

    /**
     * Build JSON object by deserializing the JSON file under given path.
     *
     * @param path path to JSON file as resource.
     * @return JSON object as result of deserializing a JSON file.
     *
     * @throws IOException if an exception occurred while creating new {@code BufferedReader}.
     * @throws FileNotFoundException when the resource under given path was not found.
     * @throws IllegalStateException if needed builder members are not initialized.
     */
    public List<T> build(Path path) throws IOException, IllegalStateException {

        if (jsonObjectType == null) {
            throw new IllegalStateException("jsonObjectType was not defined");
        }
        if (typeToken == null) {
            throw new IllegalStateException("typeToken was not defined");
        }
        String sPath = '/' + path.toString();
        InputStream stream = jsonObjectType.getResourceAsStream(sPath);
        if (stream == null) {
            throw new FileNotFoundException("Unable getResourceAsStream from path: " + sPath);
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(stream))) {
            GsonBuilder builder = new GsonBuilder();
            if (deserializer != null) {
                // assume there is a single constructor with no arguments
                Constructor<?> constructor = deserializer.getDeclaredConstructor();
                builder.registerTypeAdapter(jsonObjectType, constructor.newInstance());
            }
            // convert JSON array to list of users
            return builder.create().fromJson(reader, typeToken.getType());
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build JSON object by deserializing the JSON from given string.
     *
     * @param jsonContent string to deserialize.
     * @return JSON object as result of deserializing the string.
     *
     * @throws IllegalStateException if needed builder members are not initialized.
     */
    public List<T> build(String jsonContent) throws IllegalStateException {

        GsonBuilder builder = new GsonBuilder();
        if (deserializer != null) {
            if (jsonObjectType == null) {
                throw new IllegalStateException("jsonObjectType was not defined");
            }
            try {
                // assume there is a single constructor with no arguments
                Constructor<?> constructor = deserializer.getDeclaredConstructor();
                builder.registerTypeAdapter(jsonObjectType, constructor.newInstance());
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        if (typeToken == null) {
            throw new IllegalStateException("typeToken was not defined");
        }
        // convert JSON array to list of users
        return builder.create().fromJson(jsonContent, typeToken.getType());
    }
}
