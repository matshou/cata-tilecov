package io.matshou.cata.tilecov.json;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents Cataclysm JSON object data structure for objects that can have tiles.
 * Information here is needed to generate tile coverage metrics.
 */
@SuppressWarnings("unused")
public class CataJsonObject {

    private @Nullable String type, id, name, description;

    @SerializedArrayName("color")
    private @Nullable java.util.List<String> fgColor;

    @SerializedArrayName("bgcolor")
    private @Nullable java.util.List<String> bgColor;

    @SerializedName(value = "looks_like")
    private @Nullable String looksLike;

    @SerializedName(value = "copy-from")
    private @Nullable String copyFrom;

    @Override
    public String toString() {
        java.util.Map<String, String> mappedData = ImmutableMap.of(
                "type", getType(), "id", getId(), "name", getName(),
                "desc", getDescription(), "color", getForegroundColor().toString(),
                "bgcolor", getBackgroundColor().toString(),
                "looks_like", looksLikeWhat(), "copy-from", copyFromWhat()
        );
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : mappedData.entrySet()) {
            sb.append(entry.getKey()).append(": \"").append(entry.getValue()).append("\", ");
        }
        sb.delete(sb.length() - 2, sb.length() - 1);
        return sb.toString();
    }

    /**
     * @return type of object such as furniture, terrain, item etc.
     * or an empty string if type is not defined.
     */
    public String getType() {
        return type != null ? type : "";
    }

    /**
     * @return id of the object, this will be unique among all objects of that type
     * or an empty string if id is not defined.
     */
    public String getId() {
        return id != null ? id : "";
    }

    /**
     * @return display name of the object or an empty string if name is not defined.
     */
    public String getName() {
        return name != null ? name : "";
    }

    /**
     * @return in-game description of the object
     * or an empty string if description is not defined.
     */
    public String getDescription() {
        return description != null ? description : "";
    }

    /**
     * @return foreground color of the object as it appears in the game
     * or an empty list if colors are not defined. The list should be expected
     * to hold up to a maximum of 4 entries (one color for each calendar season).
     */
    public ImmutableList<String> getForegroundColor() {
        return fgColor != null ? ImmutableList.copyOf(fgColor) : ImmutableList.of();
    }

    /**
     * @return solid background color of the object
     * or an empty list if colors are not defined. The list should be expected
     * to hold up to a maximum of 4 entries (one color for each calendar season).
     */
    public ImmutableList<String> getBackgroundColor() {
        return bgColor != null ? ImmutableList.copyOf(bgColor) : ImmutableList.of();
    }

    /**
     * Identification of a similar item that this item looks like
     * or an empty string if this property is not defined.
     * <p>
     * The tileset loader will try to load the tile
     * for that item if this item doesn't have a tile. {@code looks_like} entries are implicitly chained, so if
     * {@code 'throne'} has {@code looks_like} {@code 'big_chair'} and {@code 'big_chair'} has {@code looks_like}
     * {code 'chair'}, a throne will be displayed using the chair tile if tiles for {@code throne} and
     * {@code big_chair} do not exist. If a tileset can't find a tile for any item in the {@code looks_like}
     * chain, it will default to the ascii symbol.
     */
    public String looksLikeWhat() {
        return looksLike != null ? looksLike : "";
    }

    /**
     * Identification of an object to copy or inherit properties from
     * or an empty string if this property is not defined.
     * <p>
     * When this property is defined all properties of the specified object that are not
     * defined in this object will be copied or inherited by the object that holds this property.
     */
    public String copyFromWhat() {
        return copyFrom != null ? copyFrom : "";
    }
}
