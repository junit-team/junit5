package com.example;

import org.junit.gen5.api.Test;

class SampleTest {

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
