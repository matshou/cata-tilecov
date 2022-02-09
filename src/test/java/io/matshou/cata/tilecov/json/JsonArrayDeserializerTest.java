package io.matshou.cata.tilecov.json;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("SpellCheckingInspection")
public class JsonArrayDeserializerTest {

	@SuppressWarnings("unused")
	private static class TestJsonObject {

		// this property is never an array of strings
		@SerializedArrayName("nonarrayprop")
		private List<String> nonArrayProperty;

		// this property can be both a string and an array of strings
		@SerializedArrayName("arrayorstringprop")
		private List<String> arrayOrStringProperty;

		// the property names in annotation will not exist
		// so this is expected to throw an exception when the property is not an array
		@SerializedArrayName("pseudoproperty")
		private List<String> mismatchedProperty;

		// this property can be both a string and an array of strings,
		// but it hasn't been annotated with SerializedArrayName annotation
		private List<String> notAnnotatedProperty;
	}

	public static class TestJsonDeserializer extends JsonArrayDeserializer<TestJsonObject> {

		TestJsonDeserializer() {
			super(TestJsonObject.class);
		}
	}

	@Test
	void shouldDeserializeNonArrayProperty() {

		String jsonString = JsonObjectTestHelper.createJsonString(Map.of(
				"nonarrayprop", "value"
		));
		List<TestJsonObject> jsonObjects =
				JsonObjectBuilder.<TestJsonObject>create()
						.ofType(TestJsonObject.class)
						.withDeserializer(TestJsonDeserializer.class)
						.withListTypeToken(new TypeToken<>() {})
						.buildAsList(jsonString);

		Assertions.assertEquals(1, jsonObjects.size());
		TestJsonObject jsonObject = jsonObjects.get(0);

		Assertions.assertNotNull(jsonObject.nonArrayProperty);
		Assertions.assertEquals(1, jsonObject.nonArrayProperty.size());
		Assertions.assertEquals("value", jsonObject.nonArrayProperty.get(0));
	}

	@Test
	void shouldDeserializeArrayOfStringsProperty() {

		String jsonString = JsonObjectTestHelper.createJsonString(Map.of(
				"arrayorstringprop", "[ \"value1\", \"value2\", \"value3\"]"
		));
		List<TestJsonObject> jsonObjects =
				JsonObjectBuilder.<TestJsonObject>create()
						.ofType(TestJsonObject.class)
						.withDeserializer(TestJsonDeserializer.class)
						.withListTypeToken(new TypeToken<>() {})
						.buildAsList(jsonString);

		Assertions.assertEquals(1, jsonObjects.size());
		TestJsonObject jsonObject = jsonObjects.get(0);

		List<String> expected = List.of("value1", "value2", "value3");
		Assertions.assertNotNull(jsonObject.arrayOrStringProperty);
		Assertions.assertEquals(expected.size(), jsonObject.arrayOrStringProperty.size());
		Assertions.assertFalse(jsonObject.arrayOrStringProperty.retainAll(expected));
	}

	@Test
	void shouldDeserializeAccordingToAnnotationValue() {

		String failJsonString = JsonObjectTestHelper.createJsonString(Map.of(
				"mismatchedProperty", "value"
		));
		@SuppressWarnings("CodeBlock2Expr")
		Executable shouldThrowException = () -> {
			JsonObjectBuilder.<TestJsonObject>create()
					.ofType(TestJsonObject.class)
					.withDeserializer(TestJsonDeserializer.class)
					.withListTypeToken(new TypeToken<>() {})
					.buildAsList(failJsonString);
		};
		Assertions.assertThrows(JsonSyntaxException.class, shouldThrowException);

		String noFailJsonString = JsonObjectTestHelper.createJsonString(Map.of(
				"mismatchedProperty", "[\"value1\", \"value2\"]"
		));
		List<TestJsonObject> jsonObjects =
				JsonObjectBuilder.<TestJsonObject>create()
						.ofType(TestJsonObject.class)
						.withDeserializer(TestJsonDeserializer.class)
						.withListTypeToken(new TypeToken<>() {})
						.buildAsList(noFailJsonString);

		Assertions.assertEquals(1, jsonObjects.size());
		TestJsonObject jsonObject = jsonObjects.get(0);

		List<String> expected = List.of("value1", "value2");
		Assertions.assertNotNull(jsonObject.mismatchedProperty);
		Assertions.assertEquals(expected.size(), jsonObject.mismatchedProperty.size());
		Assertions.assertFalse(jsonObject.mismatchedProperty.retainAll(expected));
	}

	@Test
	void shouldFailDeserializingNonAnnotatedMixedProperties() {

		String jsonString = JsonObjectTestHelper.createJsonString(Map.of(
				"notAnnotatedProperty", "value"
		));
		@SuppressWarnings("CodeBlock2Expr")
		Executable shouldThrowException = () -> {
			JsonObjectBuilder.<TestJsonObject>create()
					.ofType(TestJsonObject.class)
					.withDeserializer(TestJsonDeserializer.class)
					.withListTypeToken(new TypeToken<>() {})
					.buildAsList(jsonString);
		};
		Assertions.assertThrows(JsonSyntaxException.class, shouldThrowException);

		String jsonArraysString = JsonObjectTestHelper.createJsonString(Map.of(
				"notAnnotatedProperty", "[ \"value1\", \"value2\", \"value3\"]"
		));
		List<TestJsonObject> jsonObjects =
				JsonObjectBuilder.<TestJsonObject>create()
						.ofType(TestJsonObject.class)
						.withDeserializer(TestJsonDeserializer.class)
						.withListTypeToken(new TypeToken<>() {})
						.buildAsList(jsonArraysString);

		Assertions.assertEquals(1, jsonObjects.size());
		TestJsonObject jsonObject = jsonObjects.get(0);

		List<String> expected = List.of("value1", "value2", "value3");
		Assertions.assertNotNull(jsonObject.notAnnotatedProperty);
		Assertions.assertEquals(expected.size(), jsonObject.notAnnotatedProperty.size());
		Assertions.assertFalse(jsonObject.notAnnotatedProperty.retainAll(expected));
	}
}
