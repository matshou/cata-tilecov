package io.matshou.cata.tilecov.json;

import org.jetbrains.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This JSON object ({@code tiles-new}) contains tile atlas information.
 *
 * @see <a href="https://github.com/cataclysmbnteam/Cataclysm-BN/blob/upload/doc/TILESET.md#tile_config">
 * Cataclysm: Bright Nights - Tileset: tile_config</a>
 */
@SuppressWarnings("unused")
public class TileAtlasJsonObject {

    private @Nullable String file;

    @SerializedName("sprite_width")
    private int spriteWidth;

    @SerializedName("sprite_height")
    private int spriteHeight;

    @SerializedName("sprite_offset_x")
    private int spriteOffsetX;

    @SerializedName("sprite_offset_y")
    private int spriteOffsetY;

    /**
     * @return name of the tile atlas file
     * or an empty string if this property is not defined.
     */
    public String getFilename() {
        return file != null ? file : "";
    }

    /**
     * @return width of each sprite in this atlas.
     */
    public int getSpriteWidth() {
        return spriteWidth;
    }

    /**
     * @return height of each sprite in this atlas.
     */
    public int getSpriteHeight() {
        return spriteHeight;
    }

    /**
     * @return sprite offset along x-axis.
     */
    public int getSpriteOffsetX() {
        return spriteOffsetX;
    }

    /**
     * @return sprite offset along y-axis.
     */
    public int getSpriteOffsetY() {
        return spriteOffsetY;
    }
}
