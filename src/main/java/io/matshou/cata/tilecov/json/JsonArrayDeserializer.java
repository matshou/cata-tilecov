package io.matshou.cata.tilecov.json;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Contract;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * This class represents a custom deserializer for Json that is able to handle
 * properties that can be both string and arrays. Fields that need to be serialized
 * in such a manner should be annotated with {@link SerializedArrayName}.
 * <p>
 * Note that implementation class has to have a constructor declared with public or package access
 * so a new instance of it can be created via reflection by {@link JsonObjectBuilder} class.
 *
 * @param <T> type for which the deserializer is being registered.
 */
abstract class JsonArrayDeserializer<T> implements JsonDeserializer<T> {

    /**
     * Class that represents an object using this deserializer.
     */
    final Class<T> jsonObjectClass;

    /**
     * Fields that point to JSON properties that can be both strings and array of strings
     * mapped to JSON property keys we expect to find while parsing JSON.
     *
     * @see SerializedArrayName
     */
    final ImmutableMap<String, Field> jsonArrayFields;

    JsonArrayDeserializer(Class<T> objectClass) {
        jsonObjectClass = objectClass;

        Map<String, Field> result = new java.util.HashMap<>();
        for (Field field : jsonObjectClass.getDeclaredFields()) {
            SerializedArrayName[] annotations = field.getAnnotationsByType(SerializedArrayName.class);
            if (annotations.length == 0) {
                continue;
            }
            result.put(annotations[0].value(), field);
            if (!field.getType().equals(List.class)) {
                String message = String.format(
                        "Field %s in %s has invalid Class type (%s). Class types of " +
                                "members annotated with %s needs to be of type java.util.List.",
                        field.getName(), jsonObjectClass.getSimpleName(),
                        field.getType().getName(), SerializedArrayName.class.getSimpleName()
                );
                throw new IllegalStateException(message);
            }
            // make field accessible, so it can be changed by this class
            field.setAccessible(true);
        }
        jsonArrayFields = ImmutableMap.copyOf(result);
    }

    /**
     * Convert an element in given {@code JsonObject} to either a {@code String} or list of strings
     * depending on whether the found entry is a json array or a standard element.
     *
     * @param gson {@code GSon} used in deserializing.
     * @param jsonObject object to search for given entry element.
     * @param target target of array deserialization.
     * @param entry name of the json element to deserialize.
     */
    @Contract(mutates = "param3")
    void deserializeArrayMembers(Gson gson, JsonObject jsonObject, T target, String entry) {

        JsonElement element = jsonObject.get(entry);
        if (element != null) {
            List<String> result;
            if (!element.isJsonArray()) {
                result = List.of(gson.fromJson(element, String.class));
            }
            else {
                result = gson.fromJson(element, new TypeToken<>() {}.getType());
            }
            try {
                // no need to check for key presence, already checked by caller
                //noinspection ConstantConditions
                jsonArrayFields.get(entry).set(target, result);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

        Gson gson = new Gson();
        JsonObject jsonObject = json.getAsJsonObject();
        T targetObject = gson.fromJson(json, jsonObjectClass);

        // handle json properties that can be both string and array of string
        for (String entry : jsonArrayFields.keySet()) {
            deserializeArrayMembers(gson, jsonObject, targetObject, entry);
        }
        return targetObject;
    }
}
