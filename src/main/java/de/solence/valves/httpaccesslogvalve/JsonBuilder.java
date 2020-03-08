package de.solence.valves.httpaccesslogvalve;

/**
 * A simple JSON builder based on a StringBuilder. Only the bare minimum JSON is
 * needed to construct messages, so this avoids dependencies and minimizes
 * processing time added by generic parsers.
 * 
 * @author Robin Seggelmann
 *
 */
public class JsonBuilder {
	private StringBuilder sb;
	private boolean needSeparator;

	/**
	 * Constructor.
	 */
	public JsonBuilder() {
		sb = new StringBuilder();
		needSeparator = false;
	}

	/**
	 * Adds a JSON key/value pair.
	 * 
	 * @param key   The key.
	 * @param value The value as {@link String}.
	 * @return This object for builder pattern.
	 */
	public JsonBuilder append(String key, String value) {
		addSeparator();
		sb.append('"').append(key).append('"');
		sb.append(':');
		sb.append('"').append(value).append('"');
		needSeparator = true;
		return this;
	}

	/**
	 * Adds a JSON key/value pair.
	 * 
	 * @param key   The key.
	 * @param value The value as {@link Long}.
	 * @return This object for builder pattern.
	 */
	public JsonBuilder append(String key, long value) {
		addSeparator();
		sb.append('"').append(key).append('"');
		sb.append(':');
		sb.append(value);
		needSeparator = true;
		return this;
	}

	/**
	 * Starts a JSON object. If key is provided, a named object will be created. If
	 * key is null the object will be unnamed. All the following entries will be
	 * part of this object until it is ended.
	 * 
	 * @param key The name of the object.
	 * @return This object for builder pattern.
	 */
	public JsonBuilder startObject(String key) {
		addSeparator();
		if (key != null) {
			sb.append('"').append(key).append('"');
			sb.append(':');
		}
		sb.append('{');
		needSeparator = false;
		return this;
	}

	/**
	 * Ends a JSON object.
	 * 
	 * @return This object for builder pattern.
	 */
	public JsonBuilder endObject() {
		sb.append('}');
		needSeparator = true;
		return this;
	}

	/**
	 * Starts a JSON array. If key is provided, a named object will be created. If
	 * key is null the object will be unnamed. All the following entries will be
	 * part of this array until it is ended.
	 * 
	 * @param key The name of the object.
	 * @return This object for builder pattern.
	 */
	public JsonBuilder startArray(String key) {
		addSeparator();
		if (key != null) {
			sb.append('"').append(key).append('"');
			sb.append(':');
		}
		sb.append('[');
		needSeparator = false;
		return this;
	}

	/**
	 * Ends a JSON arrays.
	 * 
	 * @return This object for builder pattern.
	 */
	public JsonBuilder endArray() {
		sb.append(']');
		needSeparator = true;
		return this;
	}

	/**
	 * Returns the constructed JSON as a string.
	 */
	public String toString() {
		return sb.toString();
	}

	/**
	 * Adds a separator between key/value pairs, objects, and arrays, if necessary.
	 */
	private void addSeparator() {
		if (needSeparator) {
			sb.append(',');
		}
	}
}
