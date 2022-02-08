package io.matshou.cata.tilecov.json;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * This deserializer is used to deserialize {@link TileConfigJsonObject} from JSON.
 */
public class TileConfigJsonDeserializer extends JsonObjectDeserializer<TileConfigJsonObject> {

    TileConfigJsonDeserializer() {
        super(TileConfigJsonObject.class);
    }

    private void changeFieldValue(Field field, TileConfigJsonObject target, Object value) {

        try {
            field.set(target, value);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void deserializeTileInfo(TileConfigJsonObject target, JsonElement element) {

        List<TileInfoJsonObject> jsonObjects = JsonObjectBuilder.<TileInfoJsonObject>create()
                .ofType(TileInfoJsonObject.class)
                .withListTypeToken(new TypeToken<>() {})
                .buildAsList(element.toString());

        Field field = Objects.requireNonNull(jsonObjectFields.get("tile_info"));
        changeFieldValue(field, target, jsonObjects.get(0));
    }

    private void deserializeTileAtlases(TileConfigJsonObject target, JsonElement element) {

        List<TileAtlasJsonObject> jsonObjects = JsonObjectBuilder.<TileAtlasJsonObject>create()
                .ofType(TileAtlasJsonObject.class)
                .withListTypeToken(new TypeToken<>() {})
                .buildAsList(element.toString());

        Field field = Objects.requireNonNull(jsonObjectFields.get("tiles-new"));
        changeFieldValue(field, target, jsonObjects);
    }

    @Override
    void deserializeObjectMembers(Gson gson, JsonObject jsonObject, TileConfigJsonObject target, String entry) {

        JsonElement element = jsonObject.get(entry);
        if (element == null) {
            return;
        }
        if (entry.equals("tile_info")) {
            deserializeTileInfo(target, element);
        }
        else if (entry.equals("tiles-new")) {
            deserializeTileAtlases(target, element);
        }
    }
}
