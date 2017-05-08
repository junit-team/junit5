/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.container;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.dynamicTestRegistered;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.engine;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests for {@link TestFactory @TestFactory} and {@link DynamicTest}.
 *
 * @since 5.0
 */
class DynamicTestGenerationTests extends AbstractJupiterTestEngineTests {

	@Test
	void testFactoryMethodsAreCorrectlyDiscoveredForClassSelector() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(MyDynamicTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(6, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void testFactoryMethodIsCorrectlyDiscoveredForMethodSelector() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(MyDynamicTestCase.class, "dynamicStream")).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void dynamicTestsAreExecutedFromStream() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(MyDynamicTestCase.class, "dynamicStream")).build();

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
	void dynamicTestsAreExecutedFromArray() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(MyDynamicTestCase.class, "dynamicArray")).build();

		assertAllMyDynamicTestAreExecuted(executeTests(request));
	}

	@Test
	void dynamicTestsAreExecutedFromCollection() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(MyDynamicTestCase.class, "dynamicCollection")).build();

		assertAllMyDynamicTestAreExecuted(executeTests(request));
	}

	@Test
	void dynamicTestsAreExecutedFromIterator() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(MyDynamicTestCase.class, "dynamicIterator")).build();

		assertAllMyDynamicTestAreExecuted(executeTests(request));
	}

	@Test
	void dynamicTestsAreExecutedFromIterable() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(MyDynamicTestCase.class, "dynamicIterable")).build();

		assertAllMyDynamicTestAreExecuted(executeTests(request));
	}

	private void assertAllMyDynamicTestAreExecuted(ExecutionEventRecorder eventRecorder) {
		assertAllRecordedEventCounts(eventRecorder, 3, 2, 2, 1, 1, 3);
	}

	private void assertAllRecordedEventCounts(ExecutionEventRecorder eventRecorder, int... expected) {
		// @TestFactory methods are counted as both container and test
		assertAll( //
			() -> assertEquals(expected[0], eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(expected[1], eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(expected[2], eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(expected[3], eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(expected[4], eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(expected[5], eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	private static class MyDynamicTestCase {

		private static final List<DynamicTest> list = Arrays.asList(
			dynamicTest("succeedingTest", () -> assertTrue(true, "succeeding")),
			dynamicTest("failingTest", () -> fail("failing")));

		@TestFactory
		DynamicTest[] dynamicArray() {
			return list.toArray(new DynamicTest[0]);
		}

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

	}

	@Test
	void dynamicNodesWithRequiredTestsAreSuccessful() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(RequiredDynamicTestCase.class, "successful")).build();

		assertAllRecordedEventCounts(executeTests(request), 4, 6, 6, 6, 0, 4);
	}

	@Test
	void dynamicNodesWithRequiredTestsFailingLogin() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(RequiredDynamicTestCase.class, "failLogin")).build();

		assertAllRecordedEventCounts(executeTests(request), 3, 2, 2, 1, 1, 3);
	}

	@Test
	void dynamicNodesWithRequiredTestsFailingPage() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(RequiredDynamicTestCase.class, "failPage")).build();

		assertAllRecordedEventCounts(executeTests(request), 4, 4, 4, 3, 1, 4);
	}

	private static class RequiredDynamicTestCase {

		private final Executable empty = () -> {
		};

		private DynamicNode[] dynamicNodesWithRequiredTests(Optional<String> failLogin, Optional<String> failPage) {
			return new DynamicNode[] { //
					dynamicTest("Visit page requiring authorization while not logged in", empty),
					dynamicTest("Log-in", () -> failLogin.ifPresent(Assertions::fail), state -> state),
					dynamicContainer("Can access several pages while logged in",
						dynamicTest("Visit second page", empty),
						dynamicTest("Visit third page", () -> failPage.ifPresent(Assertions::fail), state -> state),
						dynamicTest("Visit fourth page", empty)),
					dynamicTest("Log-out", empty) //
			};
		}

		@TestFactory
		DynamicNode[] successful() {
			return dynamicNodesWithRequiredTests(Optional.empty(), Optional.empty());
		}

		@TestFactory
		DynamicNode[] failLogin() {
			return dynamicNodesWithRequiredTests(Optional.of("you shall not pass"), Optional.empty());
		}

		@TestFactory
		DynamicNode[] failPage() {
			return dynamicNodesWithRequiredTests(Optional.empty(), Optional.of("page not rendered as expected"));
		}

	}

}
