package io.matshou.cata.tilecov.json;

import java.util.Map;

public class JsonObjectTestHelper {

	static String createJsonString(Map<String, String> properties) {

		StringBuilder sb = new StringBuilder();
		sb.append("[\n\t{\n\t\t");

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String value = entry.getValue();
			if (!value.startsWith("[") && !value.endsWith("]")) {
				value = '\"' + value + '\"';
			}
			sb.append('\"').append(entry.getKey()).append("\": ").append(value).append(",\n\t\t");
		}
		sb.delete(sb.length() - 4, sb.length() - 1);
		return sb.append("\n}\n]").toString();
	}

	static String createJsonString(String arrayName, Map<String, String> properties) {

		String jsonProperties = createJsonString(properties);
		return "{\n\t\"" + arrayName + "\":" + jsonProperties + "\n}";
	}
}
