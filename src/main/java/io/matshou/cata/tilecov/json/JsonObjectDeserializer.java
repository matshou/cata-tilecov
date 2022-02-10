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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

abstract class JsonObjectDeserializer<T> extends JsonArrayDeserializer<T> {

	/**
	 * Fields that point to JSON properties that need to be deserialized into
	 * dedicated Class objects other than {@code String} or {@code List},
	 *
	 * @see SerializedObjectName
	 */
	final ImmutableMap<String, Field> jsonObjectFields;

	JsonObjectDeserializer(Class<T> objectClass) {
		super(objectClass);

		Map<String, Field> result = new java.util.HashMap<>();
		for (Field field : jsonObjectClass.getDeclaredFields()) {
			SerializedObjectName[] annotations = field.getAnnotationsByType(SerializedObjectName.class);
			if (annotations.length == 0) {
				continue;
			}
			result.put(annotations[0].value(), field);
			// make field accessible, so it can be changed by this class
			field.setAccessible(true);
		}
		jsonObjectFields = ImmutableMap.copyOf(result);
	}

	/**
	 * Change the value of given {@code Field} owned by object to specified value.
	 *
	 * @param field {@code Field} to change the value of.
	 * @param object the object whose field should be modified.
	 * @param value the new value for the field of obj being modified.
	 */
	void changeFieldValue(Field field, T object, @Nullable Object value) {

		try {
			field.set(object, value);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Deserialize JSON property with given name from specified {@code JsonElement}.
	 *
	 * @param element {@code JsonElement} to deserialize.
	 * @param target instance of {@code T} that will store the result.
	 * @param name name of the property to find and deserialize.
	 */
	void deserializeObjectProperty(JsonElement element, T target, String name) {

		Field field = Objects.requireNonNull(jsonObjectFields.get(name));
		if (element.isJsonPrimitive()) {
			changeFieldValue(field, target, new JsonObjectProperty(element.getAsString()));
			return;
		}
		Optional<JsonObjectProperty> jsonObjectProperty = JsonObjectBuilder.<JsonObjectProperty>create()
				.ofType(JsonObjectProperty.class)
				.withTypeToken(new TypeToken<>() {})
				.build(element.toString());

		if (jsonObjectProperty.isEmpty()) {
			throw new NullJsonObjectException(name, TilesJsonObject.class);
		}
		changeFieldValue(field, target, jsonObjectProperty.get());
	}

	/**
	 * Deserialize target fields annotated with {@link SerializedObjectName} annotation.
	 *
	 * @param gson {@code GSon} used in deserializing.
	 * @param object object to search for given entry element.
	 * @param target target of array deserialization.
	 * @param entry name of the json element to deserialize.
	 *
	 * @throws NullJsonObjectException when deserializing JSON object unexpectedly returned {@code null}.
	 */
	@Contract(mutates = "param3")
	abstract void deserializeObjectMembers(Gson gson, JsonObject object, T target, String entry);

	@Override
	public T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

		Gson gson = new Gson();
		JsonObject jsonObject = json.getAsJsonObject();
		T targetObject = gson.fromJson(json, jsonObjectClass);

		// handle json properties that need to be deserialized into custom objects
		for (String entry : jsonObjectFields.keySet()) {
			deserializeObjectMembers(gson, jsonObject, targetObject, entry);
		}
		// handle json properties that can be both string and array of string
		for (String entry : jsonArrayFields.keySet()) {
			deserializeArrayMembers(gson, jsonObject, targetObject, entry);
		}
		return targetObject;
	}
}
