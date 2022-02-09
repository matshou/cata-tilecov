package io.matshou.cata.tilecov.json;

/**
 * This object contains basic tileset information.
 *
 * @see <a href="https://github.com/cataclysmbnteam/Cataclysm-BN/blob/upload/doc/TILESET.md#tile_config">
 * Cataclysm: Bright Nights - Tileset: tile_config</a>
 */
@SuppressWarnings("unused")
public class TileInfoJsonObject {

	private int height, width, pixelscale;
	private boolean iso;

	/**
	 * @return width of each tile in this tileset.
	 *
	 * @throws NumberFormatException if the value it not a parsable integer.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return height of each tile in this tileset.
	 *
	 * @throws NumberFormatException if the value it not a parsable integer.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Indicates an isometric tileset.
	 *
	 * @return whether this tileset is isometric. Default value is {@code false}.
	 */
	public boolean isIsometric() {
		return iso;
	}

	/**
	 * Multiplier for resizing a tileset.
	 *
	 * @return pixel scale of the tileset. Default value is {@code 1}.
	 *
	 * @throws NumberFormatException if the value it not a parsable integer.
	 */
	public int getPixelScale() {
		return pixelscale != 0 ? pixelscale : 1;
	}
}
