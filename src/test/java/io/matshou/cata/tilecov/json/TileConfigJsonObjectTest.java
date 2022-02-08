package io.matshou.cata.tilecov.json;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.reflect.TypeToken;

public class TileConfigJsonObjectTest {

    private TileConfigJsonObject tileConfig;

    @BeforeEach
    void readTileConfigFromJson() throws IOException {

        tileConfig = JsonObjectBuilder.<TileConfigJsonObject>create()
                .ofType(TileConfigJsonObject.class)
                .withTypeToken(new TypeToken<>() {})
                .withDeserializer(TileConfigJsonDeserializer.class)
                .build(Paths.get("tile_config.json"));
    }

    @Test
    void shouldDeserializeTileInfoFromJson() {

        Optional<TileInfoJsonObject> oTileInfo = tileConfig.getTileInfo();
        Assertions.assertTrue(oTileInfo.isPresent());
        TileInfoJsonObject tileInfo = oTileInfo.get();

        Assertions.assertEquals(2, tileInfo.getPixelScale());
        Assertions.assertEquals(32, tileInfo.getWidth());
        Assertions.assertEquals(32, tileInfo.getHeight());
    }

    @Test
    void shouldDeserializeTileAtlasesFromJson() {

        List<TileAtlasJsonObject> tileAtlases = tileConfig.getTileAtlases();
        Assertions.assertEquals(2, tileAtlases.size());

        TileAtlasJsonObject tiles = tileAtlases.get(0);
        TileAtlasJsonObject moreTiles = tileAtlases.get(1);

        Assertions.assertEquals("tiles.png", tiles.getFilename());
        Assertions.assertEquals("moretiles.png", moreTiles.getFilename());
    }
}
