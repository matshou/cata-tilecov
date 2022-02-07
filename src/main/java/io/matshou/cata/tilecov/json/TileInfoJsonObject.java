package io.matshou.cata.tilecov.json;

import org.jetbrains.annotations.Nullable;

/**
 * This object contains tileset metadata information.
 *
 * @see <a href="https://github.com/cataclysmbnteam/Cataclysm-BN/blob/upload/doc/TILESET.md#tile_config">
 * Cataclysm: Bright Nights - Tileset: tile_config</a>
 */
@SuppressWarnings("unused")
public class TileInfoJsonObject {

    private @Nullable String height, width, iso, pixelscale;

    /**
     * @return width of each tile in this tileset.
     * @throws NumberFormatException if the value it not a parsable integer.
     */
    public int getWidth() {
        return width != null && !width.isEmpty() ? Integer.parseInt(width) : 0;
    }

    /**
     * @return height of each tile in this tileset.
     * @throws NumberFormatException if the value it not a parsable integer.
     */
    public int getHeight() {
        return height != null && !height.isEmpty() ? Integer.parseInt(height) : 0;
    }

    /**
     * Indicates an isometric tileset.
     *
     * @return whether this tileset is isometric. Default value is {@code false}.
     */
    public boolean isIsometric() {
        return iso != null && !iso.isEmpty() && Boolean.parseBoolean(iso);
    }

    /**
     * Multiplier for resizing a tileset.
     *
     * @return pixel scale of the tileset. Default value is {@code 1}.
     * @throws NumberFormatException if the value it not a parsable integer.
     */
    public int getPixelScale() {
        return pixelscale != null ? Integer.parseInt(pixelscale) : 1;
    }
}
