package io.matshou.cata.tilecov.json;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * This object contains individual tile information.
 *
 * @see <a href="https://github.com/cataclysmbnteam/Cataclysm-BN/blob/upload/doc/TILESET.md#tile_config">
 * Cataclysm: Bright Nights - Tileset: tile_config</a>
 */
@SuppressWarnings("unused")
public class TilesJsonObject {

    @SerializedArrayName("id")
    private @Nullable List<String> tileId;

    @SerializedArrayName("fg")
    private @Nullable List<String> tileFg;

    @SerializedArrayName("bg")
    private @Nullable List<String> tileBg;

    /**
     * @return ID of the objects that will be mapped to this tile
     * or an empty list if the property has not been defined.
     */
    public ImmutableList<String> getTileId() {
        return tileId != null ? ImmutableList.copyOf(tileId) : ImmutableList.of();
    }

    /**
     * @return indexes that point to position of sprite foregrounds in sprite atlas
     * or an empty list if the property has not been defined.
     */
    public ImmutableList<String> getForegroundIndex() {
        return tileFg != null ? ImmutableList.copyOf(tileFg) : ImmutableList.of();
    }

    /**
     * @return indexes that point to position of sprite backgrounds in sprite atlas
     * or an empty list if the property has not been defined.
     */
    public ImmutableList<String> getBackgroundIndex() {
        return tileBg != null ? ImmutableList.copyOf(tileBg) : ImmutableList.of();
    }
}
