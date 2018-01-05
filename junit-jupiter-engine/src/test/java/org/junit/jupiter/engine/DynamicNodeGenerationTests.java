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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor.DYNAMIC_CONTAINER_SEGMENT_TYPE;
import static org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.container;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.displayName;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.dynamicTestRegistered;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.engine;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.isA;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

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
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

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
		assertThat(engineDescriptor.getDescendants()).as("# resolved test descriptors").hasSize(10);
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

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
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

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertAll( //
			() -> assertEquals(3, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void dynamicTestsAreExecutedFromIterator() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicIterator")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertAll( //
			() -> assertEquals(3, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void dynamicTestsAreExecutedFromIterable() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicIterable")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		// @TestFactory methods are counted as both container and test
		assertAll( //
			() -> assertEquals(3, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void singleDynamicTestIsExecutedWhenDiscoveredByUniqueId() {
		UniqueId uniqueId = discoverUniqueId(MyDynamicTestCase.class, "dynamicStream") //
				.append(DYNAMIC_TEST_SEGMENT_TYPE, "#2");

		ExecutionEventRecorder eventRecorder = executeTests(selectUniqueId(uniqueId));

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
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

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
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
			() -> assertEquals(4, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(3, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic tests registered"),
			() -> assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(4, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void singleDynamicTestInNestedDynamicContainerIsExecutedWhenDiscoveredByUniqueId() {
		UniqueId uniqueId = discoverUniqueId(MyDynamicTestCase.class, "twoNestedContainersWithTwoTestsEach") //
				.append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#1") //
				.append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#1") //
				.append(DYNAMIC_TEST_SEGMENT_TYPE, "#2");

		ExecutionEventRecorder eventRecorder = executeTests(selectUniqueId(uniqueId));

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
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

		ExecutionEventRecorder eventRecorder = executeTests(selectUniqueId(uniqueId));

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
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

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
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
			() -> assertEquals(5, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(4, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic tests registered"),
			() -> assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(5, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void legacyReportingNames() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "nestedDynamicContainers")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		// @formatter:off
		Stream<String> legacyReportingNames = eventRecorder.getExecutionEvents().stream()
				.filter(event -> event.getType() == DYNAMIC_TEST_REGISTERED)
				.map(ExecutionEvent::getTestDescriptor)
				.map(TestDescriptor::getLegacyReportingName);
		// @formatter:off
		assertThat(legacyReportingNames)
				.containsExactly("nestedDynamicContainers()[1]", "nestedDynamicContainers()[1][1]",
						"nestedDynamicContainers()[1][1][1]", "nestedDynamicContainers()[1][1][2]");
	}

	@Test
	void dynamicContainersAreExecutedFromExceptionThrowingStream() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicContainerWithExceptionThrowingStream")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertTrue(MyDynamicTestCase.exceptionThrowingStreamClosed.get(), "stream should be closed");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
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
			() -> assertEquals(4, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(3, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic tests registered"),
			() -> assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(4, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void dynamicContainersChildrenMustNotBeNull() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicContainerWithNullChildren")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
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

	}

}
