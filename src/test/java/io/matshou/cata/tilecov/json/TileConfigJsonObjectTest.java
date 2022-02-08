package io.matshou.cata.tilecov.json;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.reflect.TypeToken;

public class TileConfigJsonObjectTest {

    @Test
    void shouldDeserializeTileInfoFromJson() throws IOException {

        TileConfigJsonObject tileConfig =
                JsonObjectBuilder.<TileConfigJsonObject>create()
                        .ofType(TileConfigJsonObject.class)
                        .withTypeToken(new TypeToken<>() {})
                        .withDeserializer(TileConfigJsonDeserializer.class)
                        .build(Paths.get("tile_config.json"));

        Optional<TileInfoJsonObject> oTileInfo = tileConfig.getTileInfo();
        Assertions.assertTrue(oTileInfo.isPresent());
        TileInfoJsonObject tileInfo = oTileInfo.get();

        Assertions.assertEquals(2, tileInfo.getPixelScale());
        Assertions.assertEquals(32, tileInfo.getWidth());
        Assertions.assertEquals(32, tileInfo.getHeight());
    }
}
