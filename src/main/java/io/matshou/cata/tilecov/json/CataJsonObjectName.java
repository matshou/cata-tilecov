package io.matshou.cata.tilecov.json;

/**
 * This class represents JSON object name.
 */
public class CataJsonObjectName {

	private final String str;

	CataJsonObjectName(String value) {
		str = value;
	}

	/**
	 * @return name of the JSON object.
	 */
	public String get() {
		return str;
	}
}
