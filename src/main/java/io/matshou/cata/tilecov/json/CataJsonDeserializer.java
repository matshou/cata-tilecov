package io.matshou.cata.tilecov.json;

/**
 * This class represents a custom deserializer for {@link CataJsonObject} class.
 */
public class CataJsonDeserializer extends JsonArrayDeserializer<CataJsonObject> {

	CataJsonDeserializer() {
		super(CataJsonObject.class);
	}
}
