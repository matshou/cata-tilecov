package io.matshou.cata.tilecov.json;

import java.util.Map;

public class JsonObjectTestHelper {

    static String createJsonString(Map<String, String> properties) {

        StringBuilder sb = new StringBuilder();
        sb.append('[').append('{');

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append('}').append(']');

        return sb.toString();
    }
}
