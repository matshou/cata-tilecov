package io.matshou.cata.tilecov.json;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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

    private static class TestJsonObjectDeserializer implements JsonDeserializer<TestJsonObject> {

        @Override
        public TestJsonObject deserialize(JsonElement json, Type type,
                                          JsonDeserializationContext context) throws JsonParseException {

            TestJsonObject jsonObject = new Gson().fromJson(json, TestJsonObject.class);

            jsonObject.jsonProperty = "2";
            jsonObject.pseudoProperty = "value";

            return jsonObject;
        }
    }

    @Test
    void shouldThrowExceptionWhenResourceFileNotFound() {

        Executable constructWithPseudoPath = () ->
                JsonObjectBuilder.<TestJsonObject>create()
                        .ofType(TestJsonObject.class)
                        .withTypeToken(new TypeToken<>() {})
                        .build(Paths.get("pseudo.json"));

        Assertions.assertThrows(FileNotFoundException.class, constructWithPseudoPath);
    }

    @Test
    void shouldThrowExceptionWhenMembersNotInitialized() {

        // type was not initialized
        Executable constructWithoutType = () ->
                JsonObjectBuilder.<TestJsonObject>create()
                        .withTypeToken(new TypeToken<>() {})
                        .build(Paths.get("pseudo.json"));

        Assertions.assertThrows(IllegalStateException.class, constructWithoutType);

        constructWithoutType = () ->
                JsonObjectBuilder.<TestJsonObject>create()
                        .withDeserializer(TestJsonObjectDeserializer.class)
                        .withTypeToken(new TypeToken<>() {})
                        .build("");

        Assertions.assertThrows(IllegalStateException.class, constructWithoutType);

        // token was not initialized
        Executable constructWithoutToken = () ->
                JsonObjectBuilder.<TestJsonObject>create()
                        .ofType(TestJsonObject.class)
                        .build(Paths.get("pseudo.json"));

        Assertions.assertThrows(IllegalStateException.class, constructWithoutToken);

        constructWithoutToken = () ->
                JsonObjectBuilder.<TestJsonObject>create()
                        .ofType(TestJsonObject.class)
                        .build("");

        Assertions.assertThrows(IllegalStateException.class, constructWithoutToken);
    }

    @Test
    void shouldDeserializeSinglePropertyToClassObject() {

        String jsonString = JsonObjectTestHelper.createJsonString(
                Map.of("jsonProperty", "1")
        );
        List<TestJsonObject> jsonObjects = JsonObjectBuilder.<TestJsonObject>create()
                .ofType(TestJsonObject.class)
                .withTypeToken(new TypeToken<>() {})
                .build(jsonString);

        TestJsonObject jsonObject = jsonObjects.get(0);
        Assertions.assertEquals(1, jsonObjects.size());
        Assertions.assertEquals("1", jsonObject.jsonProperty);
        Assertions.assertNull(jsonObject.pseudoProperty);
    }

    @Test
    void shouldSupportCustomDeserializer() {

        String jsonString = String.join("",
                List.of("[", "{", "}", "]")
        );
        List<TestJsonObject> jsonObjects = JsonObjectBuilder.<TestJsonObject>create()
                .ofType(TestJsonObject.class)
                .withTypeToken(new TypeToken<>() {})
                .withDeserializer(TestJsonObjectDeserializer.class)
                .build(jsonString);

        TestJsonObject jsonObject = jsonObjects.get(0);
        Assertions.assertEquals("2", jsonObject.jsonProperty);
        Assertions.assertEquals("value", jsonObject.pseudoProperty);
    }
}
