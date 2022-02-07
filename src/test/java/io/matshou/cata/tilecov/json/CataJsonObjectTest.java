package io.matshou.cata.tilecov.json;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class CataJsonObjectTest {

    private static List<CataJsonObject> buildCataJsonObjects(Path path, boolean deserializer) throws IOException {

        InputStream stream = CataJsonObjectTest.class.getResourceAsStream('/' + path.toString());
        Assertions.assertNotNull(stream);

        try (Reader reader = new BufferedReader(new InputStreamReader(stream))) {
            GsonBuilder builder = new GsonBuilder();
            if (deserializer) {
                builder.registerTypeAdapter(CataJsonObject.class, new CataJsonDeserializer());
            }
            // convert JSON array to list of users
            TypeToken<List<CataJsonObject>> token = new TypeToken<>() {};
            return builder.create().fromJson(reader, token.getType());
        }
    }

    @Test
    void shouldDeserializeProperties() throws IOException {

        Path jsonPath = Paths.get("furniture.json");
        List<CataJsonObject> furnitureJsonObjects = buildCataJsonObjects(jsonPath, true);
        Assertions.assertEquals(4, furnitureJsonObjects.size());

        String[] expectedValues = {
                "furniture", "", "-", "-",
                "furniture", "f_floor_lamp", "floor lamp (no power)",
                "A tall standing lamp, plugged into a wall. This one has no power.",
                "furniture", "f_floor_lamp_off", "floor lamp (off)",
                "A tall standing lamp, meant to plug into a wall and light up a room.",
                "furniture", "f_floor_lamp_on", "floor lamp (on)",
                "A tall standing lamp, plugged into a wall."
        };
        Iterator<CataJsonObject> iter = furnitureJsonObjects.iterator();
        for (int i = 0; i < 16; i += 4) {
            CataJsonObject cataJsonObject = iter.next();

            String expectedType = expectedValues[i];
            Assertions.assertEquals(expectedType, cataJsonObject.getType());

            String expectedId = expectedValues[i + 1];
            Assertions.assertEquals(expectedId, cataJsonObject.getId());

            String expectedName = expectedValues[i + 2];
            Assertions.assertEquals(expectedName, cataJsonObject.getName());

            String expectedDescription = expectedValues[i + 3];
            Assertions.assertEquals(expectedDescription, cataJsonObject.getDescription());
        }
    }

    @Test
    void shouldDeserializeAnnotatedProperties() throws IOException {

        Path jsonPath = Paths.get("furniture.json");
        List<CataJsonObject> furnitureJsonObjects = buildCataJsonObjects(jsonPath, true);
        Assertions.assertEquals(4, furnitureJsonObjects.size());

        String[] expectedValues = {
                "", "",
                "", "f_floor_lamp_base",
                "f_floor_lamp", "f_floor_lamp_base",
                "f_floor_lamp", "f_floor_lamp_base"
        };
        Iterator<CataJsonObject> iter = furnitureJsonObjects.iterator();
        for (int i = 0; i < 8; i += 2) {
            CataJsonObject cataJsonObject = iter.next();

            String expectedLooksLike = expectedValues[i];
            Assertions.assertEquals(expectedLooksLike, cataJsonObject.looksLikeWhat());

            String expectedCopyFrom = expectedValues[i + 1];
            Assertions.assertEquals(expectedCopyFrom, cataJsonObject.copyFromWhat());
        }
    }

    @Test
    void shouldDeserializeJsonArrays() throws IOException {

        Path jsonPath = Paths.get("furniture.json");
        List<CataJsonObject> furnitureJsonObjects = buildCataJsonObjects(jsonPath, true);

        List<List<List<String>>> expected = ImmutableList.of(
                List.of(List.of("light_gray"), List.of()),
                List.of(List.of("blue"), List.of("white")),
                List.of(List.of("red"), List.of("purple", "orange", "green")),
                List.of(List.of("yellow"), List.of(""))
        );
        for (int i = 0; i < expected.size(); ++i) {
            CataJsonObject cataJsonObject = furnitureJsonObjects.get(i);
            List<List<String>> entry = expected.get(i);

            List<String> expectedColors = entry.get(0);
            List<String> expectedBgColors = entry.get(1);

            List<String> actualColors = cataJsonObject.getForegroundColor();
            Assertions.assertEquals(expectedColors.size(), actualColors.size());
            Assertions.assertFalse(new ArrayList<>(actualColors).retainAll(expectedColors));

            List<String> actualBgColors = cataJsonObject.getBackgroundColor();
            Assertions.assertEquals(expectedBgColors.size(), actualBgColors.size());
            Assertions.assertFalse(new ArrayList<>(actualBgColors).retainAll(expectedBgColors));
        }
    }

    @Test
    void shouldFailToDeserializeJsonArraysWithoutAdapter() throws IOException {

        Path jsonPath = Paths.get("furniture.json");
        for (CataJsonObject cataJsonObject : buildCataJsonObjects(jsonPath, false)) {
            Assertions.assertTrue(cataJsonObject.getForegroundColor().isEmpty());
            Assertions.assertTrue(cataJsonObject.getBackgroundColor().isEmpty());
        }
    }
}
