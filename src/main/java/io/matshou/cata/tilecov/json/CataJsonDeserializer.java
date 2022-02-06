package io.matshou.cata.tilecov.json;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import org.jetbrains.annotations.Contract;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

/**
 * This class represents a custom deserializer for {@link CataJsonData} class.
 */
public class CataJsonDeserializer implements JsonDeserializer<CataJsonData> {

    /**
     * Names that represent JSON properties that can be both strings and array of strings.
     * these cases are handled in {@link #deserializeArray(Gson, JsonObject, CataJsonData, String)}
     */
    private static final String[] ARRAY_ENTRIES = { "color", "bgcolor" };

    /**
     * Sets the class field in the instance with the given name to the specified value.
     *
     * @param instance instance of {@code CataJsonData} to change field in.
     * @param name name of the field to change.
     * @param value new value the field will have.
     */
    @Contract(mutates = "param1")
    private void setCataJsonDataField(CataJsonData instance, String name, Object value) {

        for (Field field : CataJsonData.class.getDeclaredFields()) {
            SerializedName[] annotations = field.getAnnotationsByType(SerializedName.class);
            boolean annotationMatch = annotations.length > 0 && annotations[0].value().equals(name);
            if (!field.getName().equals(name) && !annotationMatch) {
                continue;
            }
            try {
                field.set(instance, value);
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Convert an element in given {@code JsonObject} to either a {@code String} or list of strings
     * depending on whether the found entry is a json array or a standard element.
     *
     * @param gson {@code GSon} used in deserializing.
     * @param jsonObject object to search for given entry element.
     * @param cataJData target of array deserialization.
     * @param entry name of the json element to deserialize.
     */
    @Contract(mutates = "param3")
    private void deserializeArray(Gson gson, JsonObject jsonObject, CataJsonData cataJData, String entry) {

        JsonElement element = jsonObject.get(entry);
        if (element != null) {
            if (!element.isJsonArray()) {
                String result = gson.fromJson(element, String.class);
                setCataJsonDataField(cataJData, entry, List.of(result));
            }
            else {
                List<String> result = gson.fromJson(element, new TypeToken<>() {}.getType());
                setCataJsonDataField(cataJData, entry, result);
            }
        }
    }

    @Override
    public CataJsonData deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {

        Gson gson = new Gson();
        JsonObject jsonObject = arg0.getAsJsonObject();
        CataJsonData cataJData = gson.fromJson(arg0, CataJsonData.class);

        // handle json properties that can be both string and array of string
        for (String entry : ARRAY_ENTRIES) {
            deserializeArray(gson, jsonObject, cataJData, entry);
        }
        return cataJData;
    }
}
