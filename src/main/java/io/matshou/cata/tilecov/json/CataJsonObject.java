/*
 * Cata-Tilecov - Generates tile coverage reports for Cataclysm.
 * Copyright (C) 2022 Matthew Cain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.matshou.cata.tilecov.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;

/**
 * This class represents Cataclysm JSON object data structure for objects that can have tiles.
 * Information here is needed to generate tile coverage metrics.
 */
@SuppressWarnings("unused")
public class CataJsonObject implements CataIdentifiable {

	private @Nullable String type;

	@SerializedArrayName("id")
	private @Nullable List<String> objectIds;

	@SerializedObjectName("description")
	private @Nullable JsonObjectProperty objectDescription;

	@SerializedObjectName("name")
	private @Nullable JsonObjectProperty objectName;

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
				"type", getType(),
				"id", "[ " + String.join(",", getIds()) + " ]",
				"name", getName(),
				"desc", getDescription(),
				"color", getForegroundColor().toString(),
				"bgcolor", getBackgroundColor().toString(),
				"looks_like", looksLike != null ? looksLike : "",
				"copy-from", copyFromWhat()
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
	 * @return id entries associated with the object, they will be unique among all objects of that type
	 * or an empty list if id entries are not defined.
	 */
	public ImmutableList<String> getIds() {
		return objectIds != null ? ImmutableList.copyOf(objectIds) : ImmutableList.of();
	}

	/**
	 * @return display name of the object or an empty string if name is not defined.
	 */
	public String getName() {
		return objectName != null ? objectName.get() : "";
	}

	/**
	 * @return in-game description of the object
	 * or an empty string if description is not defined.
	 */
	public String getDescription() {
		return objectDescription != null ? objectDescription.get() : "";
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
	 * Optional containing the object with an identification of a similar item
	 * that this object looks like or an empty optional if this property is not defined.
	 * <p>
	 * The tileset loader will try to load the tile for that item if this item doesn't have a tile.
	 * {@code looks_like} entries are implicitly chained, so if {@code 'throne'} has {@code looks_like}
	 * {@code 'big_chair'} and {@code 'big_chair'} has {@code looks_like} {code 'chair'}, a throne will be
	 * displayed using the chair tile if tiles for {@code throne} and {@code big_chair} do not exist.
	 * If a tileset can't find a tile for any item in the {@code looks_like} chain, it will default
	 * to the ascii symbol.
	 *
	 * @param objects {@code Set} of {@code CataJsonObject} instances to search when looking up id
	 * declared in {@code looks_like} property. The search will be recursive looking through each
	 * object until an object with a valid id that matches {@code looks_like} value is found.
	 * @return the object this object looks like or this object if no property was defined or
	 * no object was matched with recursive search for inherited {@code looks_like} property value.
	 */
	public CataJsonObject looksLikeWhat(Set<CataJsonObject> objects) {

		if (looksLike == null || looksLike.isEmpty() || objects.isEmpty()) {
			return this;
		}
		for (CataJsonObject object : objects) {
			List<String> objectIds = object.getIds();
			if (!objectIds.isEmpty() && objectIds.get(0).equals(looksLike)) {
				return object.looksLikeWhat(objects);
			}
		} // no parent look-a-like object found
		return this;
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

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CataJsonObject that = (CataJsonObject) o;
		if (!getType().equals(that.getType())) {
			return false;
		}
		return getIds().equals(that.objectIds);
	}

	@Override
	public int hashCode() {
		return 31 * getType().hashCode() + getIds().hashCode();
	}
}
