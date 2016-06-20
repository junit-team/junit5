/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.engine.discovery.MethodSelector.selectMethod;
import static org.junit.platform.launcher.core.TestDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.TestDiscoveryRequest;

/**
 * Integration tests for test class hierarchy support in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
public class TestCaseWithInheritanceTests extends AbstractJupiterTestEngineTests {

	@BeforeEach
	void initStatics() {
		LocalTestCase.countBeforeInvoked = 0;
		LocalTestCase.countAfterInvoked = 0;
		AbstractTestCase.countSuperBeforeInvoked = 0;
		AbstractTestCase.countSuperAfterInvoked = 0;
	}

	@Test
	public void executeAllTestsInClass() {
		LocalTestCase.countAfterInvoked = 0;

		ExecutionEventRecorder eventRecorder = executeTestsForClass(LocalTestCase.class);

		assertEquals(6, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(3, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(1, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(2, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(6, LocalTestCase.countBeforeInvoked, "# before calls");
		assertEquals(6, LocalTestCase.countAfterInvoked, "# after calls");
		assertEquals(6, AbstractTestCase.countSuperBeforeInvoked, "# super before calls");
		assertEquals(6, AbstractTestCase.countSuperAfterInvoked, "# super after calls");
	}

	@Test
	public void executeSingleTest() {
		TestDiscoveryRequest request = request().selectors(selectMethod(LocalTestCase.class, "alwaysPasses")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	public void executeTestDeclaredInSuperClass() {
		TestDiscoveryRequest request = request().selectors(selectMethod(LocalTestCase.class, "superclassTest")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(1, LocalTestCase.countBeforeInvoked, "# after calls");
		assertEquals(1, LocalTestCase.countAfterInvoked, "# after calls");
		assertEquals(1, AbstractTestCase.countSuperBeforeInvoked, "# super before calls");
		assertEquals(1, AbstractTestCase.countSuperAfterInvoked, "# super after calls");

	}

	@Test
	public void executeTestWithExceptionThrownInAfterMethod() {
		TestDiscoveryRequest request = request().selectors(
			selectMethod(LocalTestCase.class, "throwExceptionInAfterMethod")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	// -------------------------------------------------------------------

	private static abstract class AbstractTestCase {

		static int countSuperBeforeInvoked = 0;
		static int countSuperAfterInvoked = 0;

		@BeforeEach
		void superBefore() {
			countSuperBeforeInvoked++;
		}

		@AfterEach
		void superAfter() {
			countSuperAfterInvoked++;
		}

		@Test
		void superclassTest() {
			/* no-op */
		}
	}

	private static class LocalTestCase extends AbstractTestCase {

		boolean throwExceptionInAfterMethod = false;

		static int countBeforeInvoked = 0;
		static int countAfterInvoked = 0;

		@BeforeEach
		void before() {
			countBeforeInvoked++;
			// Reset state, since the test instance is retained across all test methods;
			// otherwise, after() always throws an exception.
			this.throwExceptionInAfterMethod = false;
		}

		@AfterEach
		void after() {
			countAfterInvoked++;
			if (this.throwExceptionInAfterMethod) {
				throw new RuntimeException("Exception thrown from @AfterEach method");
			}
		}

		@Test
		void otherTest() {
			/* no-op */
		}

		@Test
		void throwExceptionInAfterMethod() {
			this.throwExceptionInAfterMethod = true;
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}

		@Test
		void aborted() {
			assumeTrue(false);
		}

		@Test
		void alwaysFails() {
			fail("#fail");
		}
	}

}
