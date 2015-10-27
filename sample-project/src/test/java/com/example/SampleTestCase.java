package com.example;

import org.junit.gen5.api.Test;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
class SampleTestCase {
	@Test
	void skippingTest() {
		throw new TestSkippedException("This test will be skipped!");
	}

	@Test
	void abortedTest() {
		throw new TestAbortedException("This test will be aborted!");
	}

	@Test
	void failingTest() {
		throw new AssertionError("This test will always fail!");
	}

	@Test
	void succeedingTest() {
		// no-op
	}
}