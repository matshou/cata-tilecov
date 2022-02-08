package io.matshou.cata.tilecov.json;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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

    @SerializedObjectName("tiles-new")
    private @Nullable List<TileAtlasJsonObject> tileAtlases;

    /**
     * @return basic tileset information.
     */
    public Optional<TileInfoJsonObject> getTileInfo() {
        return Optional.ofNullable(tileInfo);
    }

    /**
     * @return list of tile atlases contained in this tileset
     * or an empty list if the property is not defined.
     */
    public ImmutableList<TileAtlasJsonObject> getTileAtlases() {
        return tileAtlases != null ? ImmutableList.copyOf(tileAtlases) : ImmutableList.of();
    }
}
