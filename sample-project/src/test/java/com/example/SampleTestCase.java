
package com.example;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.api.Assumptions.*;

import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.Test;
import org.opentestalliance.TestSkippedException;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
class SampleTestCase {

	static boolean staticBeforeInvoked = false;

	boolean beforeInvoked = false;

	boolean afterInvoked = false;

	boolean throwExceptionInAfterMethod = false;


	@Before
	static void staticBefore() {
		staticBeforeInvoked = true;
	}

	@Before
	void before() {
		this.beforeInvoked = true;
	}

	@After
	void after() {
		this.afterInvoked = true;
		if (throwExceptionInAfterMethod) {
			throw new RuntimeException("Exception thrown from @After method");
		}
	}

	@Test
	void methodLevelCallbacks() {
		assertTrue(this.beforeInvoked, "@Before was not invoked on instance method");
		assertTrue(staticBeforeInvoked, "@Before was not invoked on static method");
		assertFalse(this.afterInvoked, "@After should not have been invoked");
		throwExceptionInAfterMethod = true;
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

	@Test(name = "custom name")
	void succeedingTest() {
		// no-op
	}

}
