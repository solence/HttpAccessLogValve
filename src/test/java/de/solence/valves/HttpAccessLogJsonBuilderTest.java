package de.solence.valves;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HttpAccessLogJsonBuilderTest {

	@Test
	public void buildKeyStringValuePair() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.append("testkey", "testvalue");
		assertEquals("\"testkey\":\"testvalue\"", builder.toString());
	}

	@Test
	public void buildKeyLongValuePair() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.append("testkey", 42);
		assertEquals("\"testkey\":42", builder.toString());
	}

	@Test
	public void buildEmptyObject() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.startObject(null).endObject();
		assertEquals("{}", builder.toString());
	}

	@Test
	public void buildNamedObject() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.startObject("testobj").endObject();
		assertEquals("\"testobj\":{}", builder.toString());
	}

	@Test
	public void buildNamedObjectWithKeyValuePair() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.startObject("testobj").append("testkey", "testvalue").endObject();
		assertEquals("\"testobj\":{\"testkey\":\"testvalue\"}", builder.toString());
	}

	@Test
	public void buildNamedObjectWithTwoKeyValuePairs() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.startObject("testobj").append("testkey1", "testvalue1").append("testkey2", "testvalue2").endObject();
		assertEquals("\"testobj\":{\"testkey1\":\"testvalue1\",\"testkey2\":\"testvalue2\"}", builder.toString());
	}

	@Test
	public void buildEmptyArray() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.startArray(null).endArray();
		assertEquals("[]", builder.toString());
	}

	@Test
	public void buildNamedArray() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.startArray("testarray").endArray();
		assertEquals("\"testarray\":[]", builder.toString());
	}

	@Test
	public void buildNamedArrayWithKeyValuePair() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.startArray("testarray").append("testkey", "testvalue").endArray();
		assertEquals("\"testarray\":[\"testkey\":\"testvalue\"]", builder.toString());
	}

	@Test
	public void buildNamedArrayWithTwoKeyValuePairs() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.startArray("testarray").append("testkey1", "testvalue1").append("testkey2", "testvalue2").endArray();
		assertEquals("\"testarray\":[\"testkey1\":\"testvalue1\",\"testkey2\":\"testvalue2\"]", builder.toString());
	}

	@Test
	public void buildNamedObjectWithNamedArrayWithKeyValuePair() {
		HttpAccessLogJsonBuilder builder = new HttpAccessLogJsonBuilder();
		builder.startObject("testobj").startArray("testarray").append("testkey", "testvalue").endArray().endObject();
		assertEquals("\"testobj\":{\"testarray\":[\"testkey\":\"testvalue\"]}", builder.toString());
	}

}
