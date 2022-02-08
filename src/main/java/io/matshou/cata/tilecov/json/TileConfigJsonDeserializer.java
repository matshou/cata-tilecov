package io.matshou.cata.tilecov.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * This deserializer is used to deserialize {@link TileConfigJsonObject} from JSON.
 */
public class TileConfigJsonDeserializer extends JsonObjectDeserializer<TileConfigJsonObject> {

    TileConfigJsonDeserializer() {
        super(TileConfigJsonObject.class);
    }

    @Override
    void deserializeObjectMembers(Gson gson, JsonObject jsonObject, TileConfigJsonObject target, String entry) {

        JsonElement element = jsonObject.get(entry);
        if (element != null) {
            List<TileInfoJsonObject> jsonObjects =
                    JsonObjectBuilder.<TileInfoJsonObject>create()
                            .ofType(TileInfoJsonObject.class)
                            .withListTypeToken(new TypeToken<>() {})
                            .buildAsList(element.toString());
            try {
                Field tileInfoField = Objects.requireNonNull(jsonObjectFields.get("tile_info"));
                tileInfoField.set(target, jsonObjects.get(0));
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
