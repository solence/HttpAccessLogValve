package de.solence.valves.httpaccesslogvalve;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.solence.valves.httpaccesslogvalve.JsonBuilder;

public class JsonBuilderTest {

	@Test
	public void buildKeyStringValuePair() {
		JsonBuilder builder = new JsonBuilder();
		builder.append("testkey", "testvalue");
		assertEquals("\"testkey\":\"testvalue\"", builder.toString());
	}

	@Test
	public void buildKeyLongValuePair() {
		JsonBuilder builder = new JsonBuilder();
		builder.append("testkey", 42);
		assertEquals("\"testkey\":42", builder.toString());
	}

	@Test
	public void buildEmptyObject() {
		JsonBuilder builder = new JsonBuilder();
		builder.startObject(null).endObject();
		assertEquals("{}", builder.toString());
	}

	@Test
	public void buildNamedObject() {
		JsonBuilder builder = new JsonBuilder();
		builder.startObject("testobj").endObject();
		assertEquals("\"testobj\":{}", builder.toString());
	}

	@Test
	public void buildNamedObjectWithKeyValuePair() {
		JsonBuilder builder = new JsonBuilder();
		builder.startObject("testobj").append("testkey", "testvalue").endObject();
		assertEquals("\"testobj\":{\"testkey\":\"testvalue\"}", builder.toString());
	}

	@Test
	public void buildNamedObjectWithTwoKeyValuePairs() {
		JsonBuilder builder = new JsonBuilder();
		builder.startObject("testobj").append("testkey1", "testvalue1").append("testkey2", "testvalue2").endObject();
		assertEquals("\"testobj\":{\"testkey1\":\"testvalue1\",\"testkey2\":\"testvalue2\"}", builder.toString());
	}

	@Test
	public void buildEmptyArray() {
		JsonBuilder builder = new JsonBuilder();
		builder.startArray(null).endArray();
		assertEquals("[]", builder.toString());
	}

	@Test
	public void buildNamedArray() {
		JsonBuilder builder = new JsonBuilder();
		builder.startArray("testarray").endArray();
		assertEquals("\"testarray\":[]", builder.toString());
	}

	@Test
	public void buildNamedArrayWithKeyValuePair() {
		JsonBuilder builder = new JsonBuilder();
		builder.startArray("testarray").append("testkey", "testvalue").endArray();
		assertEquals("\"testarray\":[\"testkey\":\"testvalue\"]", builder.toString());
	}

	@Test
	public void buildNamedArrayWithTwoKeyValuePairs() {
		JsonBuilder builder = new JsonBuilder();
		builder.startArray("testarray").append("testkey1", "testvalue1").append("testkey2", "testvalue2").endArray();
		assertEquals("\"testarray\":[\"testkey1\":\"testvalue1\",\"testkey2\":\"testvalue2\"]", builder.toString());
	}

	@Test
	public void buildNamedObjectWithNamedArrayWithKeyValuePair() {
		JsonBuilder builder = new JsonBuilder();
		builder.startObject("testobj").startArray("testarray").append("testkey", "testvalue").endArray().endObject();
		assertEquals("\"testobj\":{\"testarray\":[\"testkey\":\"testvalue\"]}", builder.toString());
	}

}
