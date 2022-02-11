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

import com.google.common.collect.ImmutableList;

/**
 * This class represents a {@link CataIdentifiable} filter
 * which can be used to filter identifiable object instances that do not meet certain conditions.
 */
public enum CataIdentifiableFilter {

	/**
	 * This filter can be used to filter out {@link CataIdentifiable}
	 * objects that do not have an id property assigned to them.
	 */
	NO_EMPTY_ID {
		@Override
		public boolean match(CataIdentifiable identifiable) {
			ImmutableList<String> idList = identifiable.getIds();
			return idList.isEmpty() || idList.stream().anyMatch(String::isEmpty);
		}
	},

	NO_OVERLAYS {
		@Override
		public boolean match(CataIdentifiable identifiable) {
			return identifiable.getIds().stream().anyMatch(i -> i.startsWith("_overlay"));
		}
	};

	/**
	 * @param identifiable object instance to match against filter.
	 * @return {@code true} if the given object matches the conditions of this filter.
	 */
	public abstract boolean match(CataIdentifiable identifiable);
}
