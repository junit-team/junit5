/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.assertAll;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.engine.ClassSelector.forClass;
import static org.junit.gen5.engine.DiscoveryRequestBuilder.request;

import org.junit.gen5.api.*;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.ExecutionEventRecorder;

/**
 * Integration tests that verify support for {@linkplain Nested nested contexts}
 * in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class NestedTestClassesTests extends AbstractJUnit5TestEngineTests {

	@Test
	public void nestedTestsAreCorrectlyDiscovered() {
		DiscoveryRequest request = request().select(forClass(TestCaseWithNesting.class)).build();
		EngineDescriptor engineDescriptor = discoverTests(request);
		assertEquals(5, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void nestedTestsAreExecuted() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(TestCaseWithNesting.class);

		assertEquals(3L, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1L, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(3L, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(3L, eventRecorder.getContainerFinishedCount(), "# containers finished");
	}

	@Test
	public void doublyNestedTestsAreCorrectlyDiscovered() {
		DiscoveryRequest request = request().select(forClass(TestCaseWithDoubleNesting.class)).build();
		EngineDescriptor engineDescriptor = discoverTests(request);
		assertEquals(8, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	public void doublyNestedTestsAreExecuted() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(TestCaseWithDoubleNesting.class);

		assertEquals(5L, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(3L, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(2L, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(4L, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(4L, eventRecorder.getContainerFinishedCount(), "# containers finished");

		assertAll("before each counts", //
			() -> assertEquals(5, TestCaseWithDoubleNesting.beforeTopCount),
			() -> assertEquals(4, TestCaseWithDoubleNesting.beforeNestedCount),
			() -> assertEquals(2, TestCaseWithDoubleNesting.beforeDoublyNestedCount));

		assertAll("after each counts", //
			() -> assertEquals(5, TestCaseWithDoubleNesting.afterTopCount),
			() -> assertEquals(4, TestCaseWithDoubleNesting.afterNestedCount),
			() -> assertEquals(2, TestCaseWithDoubleNesting.afterDoublyNestedCount));

	}

	// -------------------------------------------------------------------

	private static class TestCaseWithNesting {

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

	static private class TestCaseWithDoubleNesting {

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

}
