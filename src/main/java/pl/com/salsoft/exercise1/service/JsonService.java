package pl.com.salsoft.exercise1.service;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;

/**
 * A wrapper around Jackson's ObjectMapper, so the API is a bit simpler.
 * Mainly it gets rid of a requirement to handle checked exceptions every time.
 * Since we're dealing with in-memory data (Object<->String) there are only 2 reasons
 * why exception may be raised. Either JSON/Object is incorrect, or JVM encountered critical error.
 * Either way we can only log it (in exception handler) and interrupt the request processing.
 * No way to recover at early stage. Therefore passing this through unchecked exception is the way to go.
 *
 * This is desired behavior for purpose of this application. In other applications having checked exception
 * maybe more useful, as serialization/deserialization problems could be handled early, but not in this case.
 */
public class JsonService {
	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Serializes given object to JSON representation.
	 * @param object Object to serialize.
	 * @return JSON representation of the object. Never null.
	 * @throws IllegalArgumentException if given object could not be serialized to String.
	 */
	public @NonNull String map(final Object object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (final JsonProcessingException e) {
			throw new IllegalArgumentException("Could not serialize input object to JSON.", e);
		}
	}

	/**
	 * Deserializes JSON to object of given class.
	 * @param json JSON string representing object to be deserialized.
	 * @param cls Target class to deserialize into. The class has to provide setters or @JsonConstructor.
	 * @return Deserialized object. Never null.
	 * @throws IllegalArgumentException if given JSON could not be deserialized to object of given class.
	 */
	public <T> @NonNull T map(@NonNull final String json, final Class<T> cls) {
		try {
			return mapper.readValue(json, cls);
		} catch (final IOException e) {
			throw new IllegalArgumentException("Could not deserialize input JSON object.", e);
		}
	}
}
