package io.matshou.cata.tilecov.json;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

public class TileConfigJsonObjectTest {

    @Test
    void shouldDeserializeTileInfoFromJson() {

        String jsonString = JsonObjectTestHelper.createJsonString("tile_info", Map.of(
                "pixelscale", "1",
                "width", "32",
                "height", "32"
        ));
        TileConfigJsonObject tileConfig =
                JsonObjectBuilder.<TileConfigJsonObject>create()
                        .ofType(TileConfigJsonObject.class)
                        .withTypeToken(new TypeToken<>() {})
                        .withDeserializer(TileConfigJsonDeserializer.class)
                        .build(jsonString);

        Optional<TileInfoJsonObject> oTileInfo = tileConfig.getTileInfo();
        Assertions.assertTrue(oTileInfo.isPresent());
        TileInfoJsonObject tileInfo = oTileInfo.get();

        Assertions.assertEquals(1, tileInfo.getPixelScale());
        Assertions.assertEquals(32, tileInfo.getWidth());
        Assertions.assertEquals(32, tileInfo.getHeight());
    }
}
