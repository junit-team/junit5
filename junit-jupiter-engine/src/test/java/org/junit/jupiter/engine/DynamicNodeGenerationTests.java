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

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor.DYNAMIC_CONTAINER_SEGMENT_TYPE;
import static org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.testkit.ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED;
import static org.junit.platform.testkit.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.testkit.ExecutionEventConditions.container;
import static org.junit.platform.testkit.ExecutionEventConditions.displayName;
import static org.junit.platform.testkit.ExecutionEventConditions.dynamicTestRegistered;
import static org.junit.platform.testkit.ExecutionEventConditions.engine;
import static org.junit.platform.testkit.ExecutionEventConditions.event;
import static org.junit.platform.testkit.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.testkit.ExecutionEventConditions.started;
import static org.junit.platform.testkit.ExecutionEventConditions.test;
import static org.junit.platform.testkit.TestExecutionResultConditions.isA;
import static org.junit.platform.testkit.TestExecutionResultConditions.message;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.testkit.ExecutionEvent;
import org.junit.platform.testkit.ExecutionsResult;

/**
 * Integration tests for {@link TestFactory @TestFactory}, {@link DynamicTest},
 * and {@link org.junit.jupiter.api.DynamicContainer}.
 *
 * @since 5.0
 */
class DynamicNodeGenerationTests extends AbstractJupiterTestEngineTests {

	@Test
	void testFactoryMethodsAreCorrectlyDiscoveredForClassSelector() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(MyDynamicTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertThat(engineDescriptor.getDescendants()).as("# resolved test descriptors").hasSize(12);
	}

	@Test
	void testFactoryMethodIsCorrectlyDiscoveredForMethodSelector() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicStream")).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertThat(engineDescriptor.getDescendants()).as("# resolved test descriptors").hasSize(2);
	}

	@Test
	void dynamicTestsAreExecutedFromStream() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicStream")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("dynamicStream"), started()), //
			event(dynamicTestRegistered("dynamic-test:#1")), //
			event(test("dynamic-test:#1", "succeedingTest"), started()), //
			event(test("dynamic-test:#1", "succeedingTest"), finishedSuccessfully()), //
			event(dynamicTestRegistered("dynamic-test:#2")), //
			event(test("dynamic-test:#2", "failingTest"), started()), //
			event(test("dynamic-test:#2", "failingTest"), finishedWithFailure(message("failing"))), //
			event(container("dynamicStream"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void dynamicTestsAreExecutedFromCollection() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicCollection")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		assertAll( //
			() -> assertEquals(3, executionsResult.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2, executionsResult.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(2, executionsResult.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, executionsResult.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, executionsResult.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3, executionsResult.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void dynamicTestsAreExecutedFromIterator() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicIterator")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		assertAll( //
			() -> assertEquals(3, executionsResult.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2, executionsResult.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(2, executionsResult.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, executionsResult.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, executionsResult.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3, executionsResult.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void dynamicTestsAreExecutedFromIterable() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicIterable")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		// @TestFactory methods are counted as both container and test
		assertAll( //
			() -> assertEquals(3, executionsResult.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2, executionsResult.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(2, executionsResult.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, executionsResult.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, executionsResult.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3, executionsResult.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void singleDynamicTestIsExecutedWhenDiscoveredByUniqueId() {
		UniqueId uniqueId = discoverUniqueId(MyDynamicTestCase.class, "dynamicStream") //
				.append(DYNAMIC_TEST_SEGMENT_TYPE, "#2");

		ExecutionsResult executionsResult = executeTests(selectUniqueId(uniqueId)).getExecutionsResult();

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("dynamicStream"), started()), //
			event(dynamicTestRegistered("dynamic-test:#2")), //
			event(test("dynamic-test:#2", "failingTest"), started()), //
			event(test("dynamic-test:#2", "failingTest"), finishedWithFailure(message("failing"))), //
			event(container("dynamicStream"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void dynamicContainersAreExecutedFromIterable() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicContainerWithIterable")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("dynamicContainerWithIterable"), started()), //
			event(dynamicTestRegistered("dynamic-container:#1")), //
			event(container("dynamic-container:#1"), started()), //
			event(dynamicTestRegistered("dynamic-test:#1")), //
			event(test("dynamic-test:#1", "succeedingTest"), started()), //
			event(test("dynamic-test:#1", "succeedingTest"), finishedSuccessfully()), //
			event(dynamicTestRegistered("dynamic-test:#2")), //
			event(test("dynamic-test:#2", "failingTest"), started()), //
			event(test("dynamic-test:#2", "failingTest"), finishedWithFailure(message("failing"))), //
			event(container("dynamic-container:#1"), finishedSuccessfully()), //
			event(container("dynamicContainerWithIterable"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertAll( //
			() -> assertEquals(4, executionsResult.getContainerStartedCount(), "# container started"),
			() -> assertEquals(3, executionsResult.getDynamicTestRegisteredCount(), "# dynamic tests registered"),
			() -> assertEquals(2, executionsResult.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, executionsResult.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, executionsResult.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(4, executionsResult.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void singleDynamicTestInNestedDynamicContainerIsExecutedWhenDiscoveredByUniqueId() {
		UniqueId uniqueId = discoverUniqueId(MyDynamicTestCase.class, "twoNestedContainersWithTwoTestsEach") //
				.append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#1") //
				.append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#1") //
				.append(DYNAMIC_TEST_SEGMENT_TYPE, "#2");

		ExecutionsResult executionsResult = executeTests(selectUniqueId(uniqueId)).getExecutionsResult();

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("twoNestedContainersWithTwoTestsEach"), started()), //
			event(dynamicTestRegistered(displayName("a"))), //
			event(container(displayName("a")), started()), //
			event(dynamicTestRegistered(displayName("a1"))), //
			event(container(displayName("a1")), started()), //
			event(dynamicTestRegistered("dynamic-test:#2")), //
			event(test("dynamic-test:#2", "failingTest"), started()), //
			event(test("dynamic-test:#2", "failingTest"), finishedWithFailure(message("failing"))), //
			event(container(displayName("a1")), finishedSuccessfully()), //
			event(container(displayName("a")), finishedSuccessfully()), //
			event(container("twoNestedContainersWithTwoTestsEach"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void allDynamicTestInNestedDynamicContainerAreExecutedWhenContainerIsDiscoveredByUniqueId() {
		UniqueId uniqueId = discoverUniqueId(MyDynamicTestCase.class, "twoNestedContainersWithTwoTestsEach") //
				.append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#2") //
				.append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#1");

		ExecutionsResult executionsResult = executeTests(selectUniqueId(uniqueId)).getExecutionsResult();

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("twoNestedContainersWithTwoTestsEach"), started()), //
			event(dynamicTestRegistered(displayName("b"))), //
			event(container(displayName("b")), started()), //
			event(dynamicTestRegistered(displayName("b1"))), //
			event(container(displayName("b1")), started()), //
			event(dynamicTestRegistered("dynamic-test:#1")), //
			event(test("dynamic-test:#1", "succeedingTest"), started()), //
			event(test("dynamic-test:#1", "succeedingTest"), finishedSuccessfully()), //
			event(dynamicTestRegistered("dynamic-test:#2")), //
			event(test("dynamic-test:#2", "failingTest"), started()), //
			event(test("dynamic-test:#2", "failingTest"), finishedWithFailure(message("failing"))), //
			event(container(displayName("b1")), finishedSuccessfully()), //
			event(container(displayName("b")), finishedSuccessfully()), //
			event(container("twoNestedContainersWithTwoTestsEach"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void nestedDynamicContainersAreExecuted() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "nestedDynamicContainers")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("nestedDynamicContainers"), started()), //
			event(dynamicTestRegistered(displayName("gift wrap"))), //
			event(container(displayName("gift wrap")), started()), //
			event(dynamicTestRegistered(displayName("box"))), //
			event(container(displayName("box")), started()), //
			event(dynamicTestRegistered("dynamic-test:#1")), //
			event(test("dynamic-test:#1", "succeedingTest"), started()), //
			event(test("dynamic-test:#1", "succeedingTest"), finishedSuccessfully()), //
			event(dynamicTestRegistered("dynamic-test:#2")), //
			event(test("dynamic-test:#2", "failingTest"), started()), //
			event(test("dynamic-test:#2", "failingTest"), finishedWithFailure(message("failing"))), //
			event(container(displayName("box")), finishedSuccessfully()), //
			event(container(displayName("gift wrap")), finishedSuccessfully()), //
			event(container("nestedDynamicContainers"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertAll( //
			() -> assertEquals(5, executionsResult.getContainerStartedCount(), "# container started"),
			() -> assertEquals(4, executionsResult.getDynamicTestRegisteredCount(), "# dynamic tests registered"),
			() -> assertEquals(2, executionsResult.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, executionsResult.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, executionsResult.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(5, executionsResult.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void legacyReportingNames() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "nestedDynamicContainers")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		// @formatter:off
		Stream<String> legacyReportingNames = executionsResult.getExecutionEvents().stream()
				.filter(ExecutionEvent.byType(DYNAMIC_TEST_REGISTERED))
				.map(ExecutionEvent::getTestDescriptor)
				.map(TestDescriptor::getLegacyReportingName);
		assertThat(legacyReportingNames)
				.containsExactly("nestedDynamicContainers()[1]", "nestedDynamicContainers()[1][1]",
						"nestedDynamicContainers()[1][1][1]", "nestedDynamicContainers()[1][1][2]");
		// @formatter:on
	}

	@Test
	void dynamicContainersAreExecutedFromExceptionThrowingStream() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicContainerWithExceptionThrowingStream")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		assertTrue(MyDynamicTestCase.exceptionThrowingStreamClosed.get(), "stream should be closed");

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("dynamicContainerWithExceptionThrowingStream"), started()), //
			event(dynamicTestRegistered("dynamic-container:#1")), //
			event(container("dynamic-container:#1"), started()), //
			event(dynamicTestRegistered("dynamic-test:#1")), //
			event(test("dynamic-test:#1", "succeedingTest"), started()), //
			event(test("dynamic-test:#1", "succeedingTest"), finishedSuccessfully()), //
			event(dynamicTestRegistered("dynamic-test:#2")), //
			event(test("dynamic-test:#2", "failingTest"), started()), //
			event(test("dynamic-test:#2", "failingTest"), finishedWithFailure(message("failing"))), //
			event(container("dynamic-container:#1"), finishedWithFailure(isA(ArrayIndexOutOfBoundsException.class))), //
			event(container("dynamicContainerWithExceptionThrowingStream"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertAll( //
			() -> assertEquals(4, executionsResult.getContainerStartedCount(), "# container started"),
			() -> assertEquals(3, executionsResult.getDynamicTestRegisteredCount(), "# dynamic tests registered"),
			() -> assertEquals(2, executionsResult.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, executionsResult.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, executionsResult.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(4, executionsResult.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void dynamicContainersChildrenMustNotBeNull() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicContainerWithNullChildren")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("dynamicContainerWithNullChildren"), started()), //
			event(dynamicTestRegistered("dynamic-container:#1")), //
			event(container("dynamic-container:#1"), started()), //
			event(container("dynamic-container:#1"), //
				finishedWithFailure(message("individual dynamic node must not be null"))), //
			event(container("dynamicContainerWithNullChildren"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void testFactoryMethodsMayReturnSingleDynamicContainer() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "singleContainer")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("singleContainer"), started()), //
			event(dynamicTestRegistered("dynamic-container:#1")), //
			event(container("dynamic-container:#1"), started()), //
			event(dynamicTestRegistered("dynamic-test:#1")), //
			event(test("dynamic-test:#1", "succeedingTest"), started()), //
			event(test("dynamic-test:#1", "succeedingTest"), finishedSuccessfully()), //
			event(dynamicTestRegistered("dynamic-test:#2")), //
			event(test("dynamic-test:#2", "failingTest"), started()), //
			event(test("dynamic-test:#2", "failingTest"), finishedWithFailure(message("failing"))), //
			event(container("dynamic-container:#1"), finishedSuccessfully()), //
			event(container("singleContainer"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void testFactoryMethodsMayReturnSingleDynamicTest() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "singleTest")).build();

		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		assertRecordedExecutionEventsContainsExactly(executionsResult.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(MyDynamicTestCase.class), started()), //
			event(container("singleTest"), started()), //
			event(dynamicTestRegistered("dynamic-test:#1")), //
			event(test("dynamic-test:#1", "succeedingTest"), started()), //
			event(test("dynamic-test:#1", "succeedingTest"), finishedSuccessfully()), //
			event(container("singleTest"), finishedSuccessfully()), //
			event(container(MyDynamicTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	static class MyDynamicTestCase {

		private static final List<DynamicTest> list = Arrays.asList(
			dynamicTest("succeedingTest", () -> assertTrue(true, "succeeding")),
			dynamicTest("failingTest", () -> fail("failing")));

		private static final AtomicBoolean exceptionThrowingStreamClosed = new AtomicBoolean(false);

		@TestFactory
		Collection<DynamicTest> dynamicCollection() {
			return list;
		}

		@TestFactory
		Stream<DynamicTest> dynamicStream() {
			return list.stream();
		}

		@TestFactory
		Iterator<DynamicTest> dynamicIterator() {
			return list.iterator();
		}

		@TestFactory
		Iterable<DynamicTest> dynamicIterable() {
			return this::dynamicIterator;
		}

		@TestFactory
		Iterable<DynamicNode> dynamicContainerWithIterable() {
			return singleton(dynamicContainer("box", list));
		}

		@TestFactory
		Iterable<DynamicNode> nestedDynamicContainers() {
			return singleton(dynamicContainer("gift wrap", singleton(dynamicContainer("box", list))));
		}

		@TestFactory
		Stream<DynamicNode> twoNestedContainersWithTwoTestsEach() {
			return Stream.of( //
				dynamicContainer("a", singleton(dynamicContainer("a1", list))), //
				dynamicContainer("b", singleton(dynamicContainer("b1", list))) //
			);
		}

		@TestFactory
		Iterable<DynamicNode> dynamicContainerWithExceptionThrowingStream() {
			// @formatter:off
			return singleton(dynamicContainer("box",
					IntStream.rangeClosed(0, 100)
							.mapToObj(list::get)
							.onClose(() -> exceptionThrowingStreamClosed.set(true))));
			// @formatter:on
		}

		@TestFactory
		Iterable<DynamicNode> dynamicContainerWithNullChildren() {
			return singleton(dynamicContainer("box", singleton(null)));
		}

		@TestFactory
		DynamicNode singleContainer() {
			return dynamicContainer("box", list);
		}

		@TestFactory
		DynamicNode singleTest() {
			return dynamicTest("succeedingTest", () -> assertTrue(true));
		}

	}

}
