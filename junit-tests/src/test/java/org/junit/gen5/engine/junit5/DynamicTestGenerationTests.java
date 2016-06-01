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
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.fail;
import static org.junit.gen5.engine.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.gen5.engine.ExecutionEventConditions.container;
import static org.junit.gen5.engine.ExecutionEventConditions.dynamicTestRegistered;
import static org.junit.gen5.engine.ExecutionEventConditions.engine;
import static org.junit.gen5.engine.ExecutionEventConditions.event;
import static org.junit.gen5.engine.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.gen5.engine.ExecutionEventConditions.finishedWithFailure;
import static org.junit.gen5.engine.ExecutionEventConditions.started;
import static org.junit.gen5.engine.ExecutionEventConditions.test;
import static org.junit.gen5.engine.TestExecutionResultConditions.message;
import static org.junit.gen5.engine.discovery.ClassSelector.selectClass;
import static org.junit.gen5.engine.discovery.MethodSelector.selectMethod;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestFactory;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * Integration tests for {@link TestFactory @TestFactory} and {@link DynamicTest}.
 *
 * @since 5.0
 */
class DynamicTestGenerationTests extends AbstractJUnit5TestEngineTests {

	@Test
	void testFactoryMethodsAreCorrectlyDiscoveredForClassSelector() {
		TestDiscoveryRequest request = request().selectors(selectClass(MyDynamicTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(5, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void testFactoryMethodIsCorrectlyDiscoveredForMethodSelector() {
		TestDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicStream")).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2, engineDescriptor.allDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void dynamicTestsAreExecutedFromStream() {
		TestDiscoveryRequest request = request().selectors(
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
		TestDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicCollection")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		// @TestFactory methods are counted as both container and test
		assertAll( //
			() -> assertEquals(3, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(3, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void dynamicTestsAreExecutedFromIterator() {
		TestDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicIterator")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		// @TestFactory methods are counted as both container and test
		assertAll( //
			() -> assertEquals(3, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(3, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	@Test
	void dynamicTestsAreExecutedFromIterable() {
		TestDiscoveryRequest request = request().selectors(
			selectMethod(MyDynamicTestCase.class, "dynamicIterable")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		// @TestFactory methods are counted as both container and test
		assertAll( //
			() -> assertEquals(3, eventRecorder.getContainerStartedCount(), "# container started"),
			() -> assertEquals(2, eventRecorder.getDynamicTestRegisteredCount(), "# dynamic registered"),
			() -> assertEquals(3, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed"),
			() -> assertEquals(3, eventRecorder.getContainerFinishedCount(), "# container finished"));
	}

	private static class MyDynamicTestCase {

		private static final List<DynamicTest> list = Arrays.asList(
			new DynamicTest("succeedingTest", () -> assertTrue(true, "succeeding")),
			new DynamicTest("failingTest", () -> fail("failing")));

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
