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
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
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
		assertEquals(5, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
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
	void dynamicTestsAreExecutedFromCollection() {
		LauncherDiscoveryRequest request = request().selectors(
			DiscoverySelectors.selectMethod(MyDynamicTestCase.class, "dynamicCollection")).build();

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
			DiscoverySelectors.selectMethod(MyDynamicTestCase.class, "dynamicIterator")).build();

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
			DiscoverySelectors.selectMethod(MyDynamicTestCase.class, "dynamicIterable")).build();

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

	private static class MyDynamicTestCase {

		private static final List<DynamicTest> list = Arrays.asList(
			dynamicTest("succeedingTest", () -> assertTrue(true, "succeeding")),
			dynamicTest("failingTest", () -> fail("failing")));

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

}
