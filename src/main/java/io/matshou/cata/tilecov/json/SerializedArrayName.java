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

import java.lang.annotation.*;

/**
 * An annotation that indicates this member should be serialized to JSON
 * with the provided name value as its field name.
 * <p>
 * It should be used to annotate fields that represent array JSON values.
 * Each JSON value can either be parsed as a string or an array of strings,
 * but sometimes a property can be both at which point Gson would throw an exception
 * while parsing JSON due to expecting the property to be one single type at all times.
 * <p>
 * Members annotated with {@code SerializedArrayName} are expected to be declared as {@link java.util.List}
 * otherwise the application will throw a {@link RuntimeException} during deserialization.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface SerializedArrayName {

	/**
	 * @return the desired name of the field when it is serialized or deserialized.
	 */
	String value();
}
