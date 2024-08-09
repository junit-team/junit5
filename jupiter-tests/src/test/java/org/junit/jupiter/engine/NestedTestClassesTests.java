/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.util.Throwables.getRootCause;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.NestedTestClassesTests.OuterClass.NestedClass;
import org.junit.jupiter.engine.NestedTestClassesTests.OuterClass.NestedClass.RecursiveNestedClass;
import org.junit.jupiter.engine.NestedTestClassesTests.OuterClass.NestedClass.RecursiveNestedSiblingClass;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Events;

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
		EngineExecutionResults executionResults = executeTestsForClass(TestCaseWithNesting.class);
		Events containers = executionResults.containerEvents();
		Events tests = executionResults.testEvents();

		assertEquals(3, tests.started().count(), "# tests started");
		assertEquals(2, tests.succeeded().count(), "# tests succeeded");
		assertEquals(1, tests.failed().count(), "# tests failed");

		assertEquals(3, containers.started().count(), "# containers started");
		assertEquals(3, containers.finished().count(), "# containers finished");
	}

	@Test
	void doublyNestedTestsAreCorrectlyDiscovered() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(TestCaseWithDoubleNesting.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(8, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void doublyNestedTestsAreExecuted() {
		EngineExecutionResults executionResults = executeTestsForClass(TestCaseWithDoubleNesting.class);
		Events containers = executionResults.containerEvents();
		Events tests = executionResults.testEvents();

		assertEquals(5, tests.started().count(), "# tests started");
		assertEquals(3, tests.succeeded().count(), "# tests succeeded");
		assertEquals(2, tests.failed().count(), "# tests failed");

		assertEquals(4, containers.started().count(), "# containers started");
		assertEquals(4, containers.finished().count(), "# containers finished");

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
		EngineExecutionResults executionResults = executeTestsForClass(TestCaseWithInheritedNested.class);
		Events containers = executionResults.containerEvents();
		Events tests = executionResults.testEvents();

		assertEquals(3, tests.started().count(), "# tests started");
		assertEquals(2, tests.succeeded().count(), "# tests succeeded");
		assertEquals(1, tests.failed().count(), "# tests failed");

		assertEquals(4, containers.started().count(), "# containers started");
		assertEquals(4, containers.finished().count(), "# containers finished");
	}

	@Test
	void extendedNestedTestsAreExecuted() {
		EngineExecutionResults executionResults = executeTestsForClass(TestCaseWithExtendedNested.class);
		executionResults.allEvents().debug();
		Events containers = executionResults.containerEvents();
		Events tests = executionResults.testEvents();

		assertEquals(6, tests.started().count(), "# tests started");
		assertEquals(4, tests.succeeded().count(), "# tests succeeded");
		assertEquals(2, tests.failed().count(), "# tests failed");

		assertEquals(8, containers.started().count(), "# containers started");
		assertEquals(8, containers.finished().count(), "# containers finished");
	}

	@Test
	void deeplyNestedInheritedMethodsAreExecutedWhenSelectedViaUniqueId() {
		EngineExecutionResults executionResults = executeTests(selectUniqueId(
			"[engine:junit-jupiter]/[class:org.junit.jupiter.engine.NestedTestClassesTests$TestCaseWithExtendedNested]/[nested-class:ConcreteInner1]/[nested-class:NestedInAbstractClass]/[nested-class:SecondLevelInherited]/[method:test()]"),
			selectUniqueId(
				"[engine:junit-jupiter]/[class:org.junit.jupiter.engine.NestedTestClassesTests$TestCaseWithExtendedNested]/[nested-class:ConcreteInner2]/[nested-class:NestedInAbstractClass]/[nested-class:SecondLevelInherited]/[method:test()]"));
		executionResults.allEvents().debug();
		Events containers = executionResults.containerEvents();
		Events tests = executionResults.testEvents();

		assertEquals(2, tests.started().count(), "# tests started");
		assertEquals(2, tests.succeeded().count(), "# tests succeeded");
		assertEquals(0, tests.failed().count(), "# tests failed");

		assertEquals(8, containers.started().count(), "# containers started");
		assertEquals(8, containers.finished().count(), "# containers finished");
	}

	/**
	 * @since 1.6
	 */
	@Test
	void recursiveNestedTestClassHierarchiesAreNotExecuted() {
		assertNestedCycle(OuterClass.class, RecursiveNestedClass.class, OuterClass.class);
		assertNestedCycle(NestedClass.class, RecursiveNestedClass.class, OuterClass.class);
		assertNestedCycle(RecursiveNestedClass.class, RecursiveNestedClass.class, OuterClass.class);
	}

	/**
	 * NOTE: We do not actually support this as a feature, but we currently only
	 * check for cycles if a class is selected. Thus, the tests in this method
	 * pass, since the selection of a particular method does not result in a
	 * lookup for nested test classes.
	 *
	 * @since 1.6
	 */
	@Test
	void individualMethodsWithinRecursiveNestedTestClassHierarchiesAreExecuted() {
		EngineExecutionResults executionResults = executeTests(selectMethod(OuterClass.class, "outer"));
		executionResults.containerEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
		executionResults.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));

		executionResults = executeTests(selectMethod(NestedClass.class, "nested"));
		executionResults.containerEvents().assertStatistics(stats -> stats.started(3).succeeded(3));
		executionResults.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));

		executionResults = executeTests(selectMethod(RecursiveNestedClass.class, "nested"));
		executionResults.containerEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
		executionResults.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));

		executionResults = executeTests(selectMethod(RecursiveNestedSiblingClass.class, "nested"));
		executionResults.containerEvents().assertStatistics(stats -> stats.started(4).succeeded(4));
		executionResults.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
	}

	private void assertNestedCycle(Class<?> start, Class<?> from, Class<?> to) {
		assertThatExceptionOfType(JUnitException.class)//
				.isThrownBy(() -> executeTestsForClass(start))//
				.withCauseExactlyInstanceOf(JUnitException.class)//
				.satisfies(ex -> assertThat(getRootCause(ex)).hasMessageMatching(
					String.format("Detected cycle in inner class hierarchy between .+%s and .+%s", from.getSimpleName(),
						to.getSimpleName())));
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

			@Nested
			class SecondLevelInherited {
				@Test
				void test() {
				}
			}
		}
	}

	static class TestCaseWithInheritedNested extends AbstractSuperClass {
		// empty on purpose
	}

	static class TestCaseWithExtendedNested {
		@Nested
		class ConcreteInner1 extends AbstractSuperClass {
		}

		@Nested
		class ConcreteInner2 extends AbstractSuperClass {
		}
	}

	static class AbstractOuterClass {
	}

	static class OuterClass extends AbstractOuterClass {

		@Test
		void outer() {
		}

		@Nested
		class NestedClass {

			@Test
			void nested() {
			}

			@Nested
			class RecursiveNestedClass extends OuterClass {

				@Test
				void nested() {
				}
			}

			@Nested
			// sibling of OuterClass due to common super type
			class RecursiveNestedSiblingClass extends AbstractOuterClass {

				@Test
				void nested() {
				}
			}
		}
	}

}
