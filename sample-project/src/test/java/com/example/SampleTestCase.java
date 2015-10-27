package com.example;

import org.junit.gen5.api.Test;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
class SampleTestCase {

	@Test
	void failingTest() {
		throw new AssertionError("this test should fail");
	}

	@Test
	void succeedingTest() {
		// no-op
		System.out.println("success");
	}

}
