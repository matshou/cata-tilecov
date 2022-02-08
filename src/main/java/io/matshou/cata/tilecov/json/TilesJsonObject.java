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
    private @Nullable List<String> id;

    @SerializedArrayName("fg")
    private @Nullable List<String> fg;

    @SerializedArrayName("bg")
    private @Nullable List<String> bg;

    /**
     * @return ID of the objects that will be mapped to this tile
     * or an empty list if the property has not been defined.
     */
    public ImmutableList<String> getId() {
        return id != null ? ImmutableList.copyOf(id) : ImmutableList.of();
    }

    /**
     * @return indexes that point to position of sprite foregrounds in sprite atlas
     * or an empty list if the property has not been defined.
     */
    public ImmutableList<String> getForegroundIndex() {
        return fg != null ? ImmutableList.copyOf(fg) : ImmutableList.of();
    }

    /**
     * @return indexes that point to position of sprite backgrounds in sprite atlas
     * or an empty list if the property has not been defined.
     */
    public ImmutableList<String> getBackgroundIndex() {
        return bg != null ? ImmutableList.copyOf(bg) : ImmutableList.of();
    }
}
