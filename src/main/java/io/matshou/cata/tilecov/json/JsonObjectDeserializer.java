package io.matshou.cata.tilecov.json;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;

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
	 * Deserialize target fields annotated with {@link SerializedObjectName} annotation.
	 *
	 * @param gson {@code GSon} used in deserializing.
	 * @param jsonObject object to search for given entry element.
	 * @param target target of array deserialization.
	 * @param entry name of the json element to deserialize.
	 */
	@Contract(mutates = "param3")
	abstract void deserializeObjectMembers(Gson gson, JsonObject jsonObject, T target, String entry);

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
