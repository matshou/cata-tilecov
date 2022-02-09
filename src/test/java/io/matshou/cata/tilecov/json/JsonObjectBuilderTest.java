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

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class JsonObjectBuilderTest {

	private static class TestJsonObject {

		// this property is expected to be initialized by Gson
		private String jsonProperty;

		// this property is expected NOT to be initialized by Gson
		private String pseudoProperty;
	}

	// spotless:off
	public static class TestJsonObjectDeserializer implements JsonDeserializer<TestJsonObject> {

		@Override
		public TestJsonObject deserialize(JsonElement json, Type type,
										  JsonDeserializationContext context) throws JsonParseException {

			TestJsonObject jsonObject = new Gson().fromJson(json, TestJsonObject.class);

			jsonObject.jsonProperty = "2";
			jsonObject.pseudoProperty = "value";

			return jsonObject;
		}
	}// spotless:on

	@Test
	void shouldThrowExceptionWhenResourceFileNotFound() {

		@SuppressWarnings("CodeBlock2Expr")
		Executable constructWithPseudoPath = () -> {
			JsonObjectBuilder.<TestJsonObject>create()
					.ofType(TestJsonObject.class)
					.withListTypeToken(new TypeToken<>() {})
					.buildAsList(Paths.get("pseudo.json"));
		};
		Assertions.assertThrows(FileNotFoundException.class, constructWithPseudoPath);
	}

	@Test
	@SuppressWarnings("CodeBlock2Expr")
	void shouldThrowExceptionWhenMembersNotInitialized() {

		// type was not initialized
		Executable constructWithoutType = () -> {
			JsonObjectBuilder.<TestJsonObject>create()
					.withListTypeToken(new TypeToken<>() {})
					.buildAsList(Paths.get("pseudo.json"));
		};
		Assertions.assertThrows(IllegalStateException.class, constructWithoutType);

		constructWithoutType = () -> {
			JsonObjectBuilder.<TestJsonObject>create()
					.withDeserializer(TestJsonObjectDeserializer.class)
					.withListTypeToken(new TypeToken<>() {})
					.buildAsList("");
		};
		Assertions.assertThrows(IllegalStateException.class, constructWithoutType);

		// token was not initialized
		Executable constructWithoutToken = () -> {
			JsonObjectBuilder.<TestJsonObject>create()
					.ofType(TestJsonObject.class)
					.buildAsList(Paths.get("pseudo.json"));
		};
		Assertions.assertThrows(IllegalStateException.class, constructWithoutToken);

		constructWithoutToken = () -> {
			JsonObjectBuilder.<TestJsonObject>create()
					.ofType(TestJsonObject.class)
					.buildAsList("");
		};
		Assertions.assertThrows(IllegalStateException.class, constructWithoutToken);
	}

	@Test
	void shouldDeserializeSinglePropertyToClassObject() throws NullJsonObjectException {

		String jsonString = JsonObjectTestHelper.createJsonString(
				Map.of("jsonProperty", "1")
		);
		Optional<List<TestJsonObject>> oJsonObjects = JsonObjectBuilder.<TestJsonObject>create()
				.ofType(TestJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.buildAsList(jsonString);

		Assertions.assertTrue(oJsonObjects.isPresent());
		List<TestJsonObject> jsonObjects = oJsonObjects.get();
		Assertions.assertEquals(1, jsonObjects.size());

		TestJsonObject jsonObject = jsonObjects.get(0);
		Assertions.assertEquals("1", jsonObject.jsonProperty);
		Assertions.assertNull(jsonObject.pseudoProperty);
	}

	@Test
	void shouldSupportCustomDeserializer() {

		String jsonString = String.join("",
				List.of("[", "{", "}", "]")
		);
		Optional<List<TestJsonObject>> oJsonObjects = JsonObjectBuilder.<TestJsonObject>create()
				.ofType(TestJsonObject.class)
				.withListTypeToken(new TypeToken<>() {})
				.withDeserializer(TestJsonObjectDeserializer.class)
				.buildAsList(jsonString);

		Assertions.assertTrue(oJsonObjects.isPresent());
		TestJsonObject jsonObject = oJsonObjects.get().get(0);
		Assertions.assertEquals("2", jsonObject.jsonProperty);
		Assertions.assertEquals("value", jsonObject.pseudoProperty);
	}
}
