
package com.example;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.api.Assumptions.*;

import org.junit.gen5.api.Before;
import org.junit.gen5.api.Test;
import org.opentestalliance.TestSkippedException;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
class SampleTestCase {

	boolean setupCalled = false;


	@Before
	void setup() {
		this.setupCalled = true;
	}

	@Test
	void methodLevelCallbacks() {
		assertTrue(this.setupCalled, "@Before was not invoked");
	}

	@Test
	void skippedTest() {
		throw new TestSkippedException("This test will be skipped");
	}

	@Test
	void abortedTest() {
		assumeTrue((2 * 3 == 4), () -> "Assumed that 2*3=4, but 2*3=" + (2 * 3));
	}

	@Test
	void failingTest() {
		fail("This test will always fail");
	}

	@Test
	void succeedingTest() {
		// no-op
	}

}
