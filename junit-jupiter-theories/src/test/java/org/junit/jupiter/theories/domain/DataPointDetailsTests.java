
package org.junit.jupiter.theories.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DataPointDetails}.
 */
class DataPointDetailsTests {
	@Test
	public void testConstructorAndGetters() {
		//Setup
		Object expectedValue = "Hello world";
		List<String> expectedQualifiers = Arrays.asList("foo", "bar", "baz");
		String expectedSourceName = "testSource";

		//Test
		DataPointDetails objectUnderTest = new DataPointDetails(expectedValue, expectedQualifiers, expectedSourceName);

		Object actualValue = objectUnderTest.getValue();
		List<String> actualQualifiers = objectUnderTest.getQualifiers();
		String actualSourceName = objectUnderTest.getSourceName();

		//Verify
		assertEquals(expectedValue, actualValue);
		assertEquals(expectedQualifiers, actualQualifiers);
		assertEquals(expectedSourceName, actualSourceName);
	}
}
