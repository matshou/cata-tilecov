package io.matshou.cata.tilecov.json;

/**
 * This exception communicates that a JSON object is {@code null} and that this is an unexpected state.
 * <p>
 * The most common situation to use this exception is to validate the result of {@link JsonObjectBuilder}
 * which will be {@code null} when the JSON being parsed was not found or is empty.
 */
public class NullJsonObjectException extends RuntimeException {

	/**
	 * Construct a new instance of this exception that communicates that a JSON property was not
	 * found in JSON object of given class which resulted in the object being {@code null}.
	 *
	 * @param jsonProperty property that was not found.
	 * @param jsonObjectClass {@code Class} of the JSON object.
	 */
	public NullJsonObjectException(String jsonProperty, Class<?> jsonObjectClass) {
		super(String.format("Unable to find expected JSON property '%s' when deserializing to class %s",
				jsonProperty, jsonObjectClass.getName()));
	}

	/**
	 * Construct a new instance of this exception that communicates that a JSON object
	 * of given class is {@code null} and that this is an unexpected state.
	 *
	 * @param jsonObjectClass {@code Class} of the JSON object.
	 */
	public NullJsonObjectException(Class<?> jsonObjectClass) {
		super(String.format("JSON object of class '%s' is null", jsonObjectClass.getName()));
	}
}
