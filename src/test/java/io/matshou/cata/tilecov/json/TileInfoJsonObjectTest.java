package io.matshou.cata.tilecov.json;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.reflect.TypeToken;

public class TileInfoJsonObjectTest {

    @Test
    void shouldDeserializeProperties() {

        String jsonString = JsonObjectTestHelper.createJsonString(Map.of(
                "pixelscale", "2",
                "width", "32",
                "height", "32",
                "iso", "true"
        ));
        List<TileInfoJsonObject> jsonObjects =
                JsonObjectBuilder.<TileInfoJsonObject>create()
                        .ofType(TileInfoJsonObject.class)
                        .withTypeToken(new TypeToken<>() {})
                        .build(jsonString);

        Assertions.assertEquals(1, jsonObjects.size());
        TileInfoJsonObject tileInfo = jsonObjects.get(0);

        Assertions.assertEquals(32, tileInfo.getHeight());
        Assertions.assertEquals(32, tileInfo.getWidth());
        Assertions.assertTrue(tileInfo.isIsometric());
        Assertions.assertEquals(2, tileInfo.getPixelScale());
    }

    @Test
    void shouldInitializeDefaultsWhenMissingProperties() {

        String jsonString = JsonObjectTestHelper.createJsonString(Map.of(
                "width", "32",
                "height", "32"
        ));
        List<TileInfoJsonObject> jsonObjects =
                JsonObjectBuilder.<TileInfoJsonObject>create()
                        .ofType(TileInfoJsonObject.class)
                        .withTypeToken(new TypeToken<>() {})
                        .build(jsonString);

        Assertions.assertEquals(1, jsonObjects.size());
        TileInfoJsonObject tileInfo = jsonObjects.get(0);

        Assertions.assertEquals(32, tileInfo.getHeight());
        Assertions.assertEquals(32, tileInfo.getWidth());
        Assertions.assertFalse(tileInfo.isIsometric());
        Assertions.assertEquals(1, tileInfo.getPixelScale());
    }

    @Test
    void shouldThrowExceptionWhenCallingGetterOnPropertyWithWrongFormat() {

        String jsonString = JsonObjectTestHelper.createJsonString(Map.of(
                "pixelscale", "true",
                "width", "string",
                "height", "string"
        ));
        List<TileInfoJsonObject> jsonObjects =
                JsonObjectBuilder.<TileInfoJsonObject>create()
                        .ofType(TileInfoJsonObject.class)
                        .withTypeToken(new TypeToken<>() {})
                        .build(jsonString);

        TileInfoJsonObject jsonObject = jsonObjects.get(0);
        Assertions.assertThrows(NumberFormatException.class, jsonObject::getPixelScale);
        Assertions.assertThrows(NumberFormatException.class, jsonObject::getWidth);
        Assertions.assertThrows(NumberFormatException.class, jsonObject::getHeight);
    }
}
