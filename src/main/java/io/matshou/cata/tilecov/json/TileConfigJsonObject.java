package io.matshou.cata.tilecov.json;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

/**
 * This class represents a tileset {@code tile_config.json} file.
 * <p>
 * Each legacy tileset has a {@code tile_config.json} describing how to map
 * the contents of a sprite sheet to various tile identifiers, different orientations, etc.
 */
@SuppressWarnings("unused")
public class TileConfigJsonObject {

    @SerializedObjectName("tile_info")
    private @Nullable TileInfoJsonObject tileInfo;

    /**
     * @return basic tileset information.
     */
    public Optional<TileInfoJsonObject> getTileInfo() {
        return Optional.ofNullable(tileInfo);
    }
}
