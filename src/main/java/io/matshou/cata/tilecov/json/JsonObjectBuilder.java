package io.matshou.cata.tilecov.json;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
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
@SuppressWarnings("ConstantConditions")
public class JsonObjectBuilder<T> {

    private @Nullable Class<T> jsonObjectType;
    private @Nullable Class<? extends JsonDeserializer<T>> deserializer;
    private @Nullable TypeToken<?> typeToken;

    /**
     * Instructs the builder to build an object of the given type.
     *
     * @param type {@code Class} representing the object that will be built.
     * @return reference to this builder.
     */
    @Contract("_ -> this")
    public JsonObjectBuilder<T> ofType(Class<T> type) {
        jsonObjectType = type;
        return this;
    }

    /**
     * Instructs the builder to expect the result of building to be a {@link List}.
     *
     * @param token {@code TypeToken} used in deserialization.
     * @return reference to this builder.
     */
    @Contract("_ -> this")
    public JsonObjectBuilder<T> withListTypeToken(TypeToken<List<T>> token) {
        typeToken = token;
        return this;
    }

    /**
     * Instructs the builder to expect the result of building to be an object.
     *
     * @param token {@code TypeToken} used in deserialization.
     * @return reference to this builder.
     */
    @Contract("_ -> this")
    public JsonObjectBuilder<T> withTypeToken(TypeToken<T> token) {
        typeToken = token;
        return this;
    }

    /**
     * Instructs the builder to use the deserializer of given {@code Class} when building.
     *
     * @param clazz {@code Class} of the deserializer to be used.
     * @return reference to this builder.
     */
    @Contract("_ -> this")
    public JsonObjectBuilder<T> withDeserializer(Class<? extends JsonDeserializer<T>> clazz) {
        deserializer = clazz;
        return this;
    }

    /**
     * Create a new instance of this builder.
     *
     * @param <T> type of object that will be the result of building.
     * @return new instance of this builder.
     */
    @Contract("-> new")
    public static <T> JsonObjectBuilder<T> create() {
        return new JsonObjectBuilder<>();
    }

    @Contract("_-> new")
    private InputStreamReader readJsonFromPath(Path path) throws IOException {

        String sPath = '/' + path.toString();
        InputStream stream = Objects.requireNonNull(jsonObjectType).getResourceAsStream(sPath);
        if (stream == null) {
            throw new FileNotFoundException("Unable getResourceAsStream from path: " + sPath);
        }
        return new InputStreamReader(stream);
    }

    @Contract("-> new")
    private GsonBuilder startBuildFromPath() {

        if (jsonObjectType == null) {
            throw new IllegalStateException("jsonObjectType was not defined");
        }
        if (typeToken == null) {
            throw new IllegalStateException("typeToken was not defined");
        }
        GsonBuilder builder = new GsonBuilder();
        try {
            if (deserializer != null) {
                // assume there is a single constructor with no arguments
                Constructor<?> constructor = deserializer.getDeclaredConstructor();
                builder.registerTypeAdapter(jsonObjectType, constructor.newInstance());
            }
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return builder;
    }

    /**
     * Build JSON object by deserializing the JSON file under given path.
     *
     * @param path path to JSON file as resource.
     * @return list of JSON objects as result of deserializing a JSON file.
     *
     * @throws IOException if an exception occurred while creating new {@code BufferedReader}.
     * @throws FileNotFoundException when the resource under given path was not found.
     * @throws IllegalStateException if needed builder members are not initialized.
     */
    public List<T> buildAsList(Path path) throws IOException, IllegalStateException {

        GsonBuilder builder = startBuildFromPath();
        try (Reader reader = readJsonFromPath(path)) {
            return builder.create().fromJson(reader, typeToken.getType());
        }
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
    public T build(Path path) throws IOException, IllegalStateException {

        GsonBuilder builder = startBuildFromPath();
        try (Reader reader = readJsonFromPath(path)) {
            return builder.create().fromJson(reader, typeToken.getType());
        }
    }

    @Contract("-> new")
    private GsonBuilder startBuildFromString() {

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
        return builder;
    }

    /**
     * Build list of JSON objects by deserializing the JSON from given string.
     * <p>
     * Note that when using this method to build you need to make sure that {@code TypeToken}
     * set for this builder is of type {@link List} otherwise an exception will be thrown.
     *
     * @param jsonContent string to deserialize.
     * @return list of JSON objects as result of deserializing the string.
     *
     * @throws IllegalStateException if needed builder members are not initialized.
     * @throws JsonSyntaxException when there was an error while building the object.
     */
    public List<T> buildAsList(String jsonContent) throws IllegalStateException {

        GsonBuilder builder = startBuildFromString();
        return builder.create().fromJson(jsonContent, typeToken.getType());
    }

    /**
     * Build JSON object by deserializing the JSON from given string.
     * <p>
     * Note that when using this method to build you need to make sure that {@code TypeToken}
     * set for this builder is NOT of type {@link List} otherwise an exception will be thrown.
     *
     * @param jsonContent string to deserialize.
     * @return JSON object as result of deserializing the string.
     *
     * @throws IllegalStateException if needed builder members are not initialized.
     * @throws JsonSyntaxException when there was an error while building the object.
     */
    public T build(String jsonContent) throws IllegalStateException {

        GsonBuilder builder = startBuildFromString();
        return builder.create().fromJson(jsonContent, typeToken.getType());
    }
}
