/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests that verify support for {@linkplain Nested nested contexts}
 * in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class NestedTestClassesTests extends AbstractJupiterTestEngineTests {

	@Test
	void nestedTestsAreCorrectlyDiscovered() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(TestCaseWithNesting.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(5, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void nestedTestsAreExecuted() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(TestCaseWithNesting.class);

		assertEquals(3, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(3, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(3, eventRecorder.getContainerFinishedCount(), "# containers finished");
	}

	@Test
	void doublyNestedTestsAreCorrectlyDiscovered() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(TestCaseWithDoubleNesting.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(8, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void doublyNestedTestsAreExecuted() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(TestCaseWithDoubleNesting.class);

		assertEquals(5, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(3, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(2, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(4, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(4, eventRecorder.getContainerFinishedCount(), "# containers finished");

		assertAll("before each counts", //
			() -> assertEquals(5, TestCaseWithDoubleNesting.beforeTopCount),
			() -> assertEquals(4, TestCaseWithDoubleNesting.beforeNestedCount),
			() -> assertEquals(2, TestCaseWithDoubleNesting.beforeDoublyNestedCount));

		assertAll("after each counts", //
			() -> assertEquals(5, TestCaseWithDoubleNesting.afterTopCount),
			() -> assertEquals(4, TestCaseWithDoubleNesting.afterNestedCount),
			() -> assertEquals(2, TestCaseWithDoubleNesting.afterDoublyNestedCount));

	}

	@Test
	void inheritedNestedTestsAreExecuted() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(TestCaseWithInheritedNested.class);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(3, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(3, eventRecorder.getContainerFinishedCount(), "# containers finished");
	}

	// -------------------------------------------------------------------

	static class TestCaseWithNesting {

		@Test
		void someTest() {
		}

		@Nested
		class NestedTestCase {

			@Test
			void successful() {
			}

			@Test
			void failing() {
				Assertions.fail("Something went horribly wrong");
			}
		}
	}

	static class TestCaseWithDoubleNesting {

		static int beforeTopCount = 0;
		static int beforeNestedCount = 0;
		static int beforeDoublyNestedCount = 0;

		static int afterTopCount = 0;
		static int afterNestedCount = 0;
		static int afterDoublyNestedCount = 0;

		@BeforeEach
		void beforeTop() {
			beforeTopCount++;
		}

		@AfterEach
		void afterTop() {
			afterTopCount++;
		}

		@Test
		void someTest() {
		}

		@Nested
		class NestedTestCase {

			@BeforeEach
			void beforeNested() {
				beforeNestedCount++;
			}

			@AfterEach
			void afterNested() {
				afterNestedCount++;
			}

			@Test
			void successful() {
			}

			@Test
			void failing() {
				Assertions.fail("Something went horribly wrong");
			}

			@Nested
			class DoublyNestedTestCase {

				@BeforeEach
				void beforeDoublyNested() {
					beforeDoublyNestedCount++;
				}

				@BeforeEach
				void afterDoublyNested() {
					afterDoublyNestedCount++;
				}

				@Test
				void successful() {
				}

				@Test
				void failing() {
					Assertions.fail("Something went horribly wrong");
				}
			}
		}
	}

	interface InterfaceWithNestedClass {

		@Nested
		class NestedInInterface {

			@Test
			void notExecutedByImplementingClass() {
				Assertions.fail("class in interface is static and should have been filtered out");
			}
		}

	}

	static abstract class AbstractSuperClass implements InterfaceWithNestedClass {

		@Nested
		class NestedInAbstractClass {

			@Test
			void successful() {
			}

			@Test
			void failing() {
				Assertions.fail("something went wrong");
			}
		}
	}

	static class TestCaseWithInheritedNested extends AbstractSuperClass {
		// empty on purpose
	}

}
