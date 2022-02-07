package io.matshou.cata.tilecov.json;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Contract;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/**
 * This class represents a custom deserializer for {@link CataJsonObject} class.
 */
public class CataJsonDeserializer implements JsonDeserializer<CataJsonObject> {

    /**
     * Fields that point to JSON properties that can be both strings and array of strings
     * mapped to JSON property keys we expect to find while parsing JSON.
     *
     * @see #deserializeArray(Gson, JsonObject, CataJsonObject, String)
     * @see SerializedArrayName
     */
    private static final ImmutableMap<String, Field> JSON_ARRAY_FIELDS;

    static {
        Map<String, Field> arrayFields = new java.util.HashMap<>();
        for (Field field : CataJsonObject.class.getDeclaredFields()) {
            SerializedArrayName[] annotations = field.getAnnotationsByType(SerializedArrayName.class);
            if (annotations.length == 0) {
                continue;
            }
            arrayFields.put(annotations[0].value(), field);
            if (!field.getType().equals(List.class)) {
                String message = String.format(
                        "Field %s in %s has invalid Class type (%s). Class types of " +
                                "members annotated with %s needs to be of type java.util.List.",
                        field.getName(), CataJsonObject.class.getSimpleName(),
                        field.getType().getName(), SerializedArrayName.class.getSimpleName()
                );
                throw new IllegalStateException(message);
            }
            // make field accessible, so it can be changed by this class
            field.setAccessible(true);
        }
        JSON_ARRAY_FIELDS = ImmutableMap.copyOf(arrayFields);
    }

    /**
     * Convert an element in given {@code JsonObject} to either a {@code String} or list of strings
     * depending on whether the found entry is a json array or a standard element.
     *
     * @param gson {@code GSon} used in deserializing.
     * @param jsonObject object to search for given entry element.
     * @param cataJsonObject target of array deserialization.
     * @param entry name of the json element to deserialize.
     */
    @Contract(mutates = "param3")
    private void deserializeArray(Gson gson, JsonObject jsonObject, CataJsonObject cataJsonObject, String entry) {

        List<String> result = new ArrayList<>();
        JsonElement element = jsonObject.get(entry);
        if (element != null) {
            if (!element.isJsonArray()) {
                result = List.of(gson.fromJson(element, String.class));
            }
            else result = gson.fromJson(element, new TypeToken<>() {}.getType());
        }
        try {
            // no need to check for key presence, already checked by caller
            //noinspection ConstantConditions
            JSON_ARRAY_FIELDS.get(entry).set(cataJsonObject, result);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CataJsonObject deserialize(JsonElement arg0, Type arg1,
                                      JsonDeserializationContext arg2) throws JsonParseException {

        Gson gson = new Gson();
        JsonObject jsonObject = arg0.getAsJsonObject();
        CataJsonObject cataJsonObject = gson.fromJson(arg0, CataJsonObject.class);

        // handle json properties that can be both string and array of string
        for (String entry : JSON_ARRAY_FIELDS.keySet()) {
            deserializeArray(gson, jsonObject, cataJsonObject, entry);
        }
        return cataJsonObject;
    }
}
