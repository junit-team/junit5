/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.engine;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.testkit.engine.EngineExecutionResults;

/**
 * Tests that verify the support for lifecycle method execution exception handling
 * via {@link LifecycleMethodExecutionExceptionHandler}
 *
 * @since 5.5
 */
class LifecycleMethodExecutionExceptionHandlerTests extends AbstractJupiterTestEngineTests {

	private static List<String> handlerCalls = new ArrayList<>();
	private static boolean throwExceptionBeforeAll;
	private static boolean throwExceptionBeforeEach;
	private static boolean throwExceptionAfterEach;
	private static boolean throwExceptionAfterAll;

	@BeforeEach
	void resetStatics() {
		throwExceptionBeforeAll = true;
		throwExceptionBeforeEach = true;
		throwExceptionAfterEach = true;
		throwExceptionAfterAll = true;
		handlerCalls.clear();

		SwallowExceptionHandler.callCounter.reset();

		RethrowExceptionHandler.callCounter.reset();

		ConvertExceptionHandler.callCounter.reset();

		UnrecoverableExceptionHandler.callCounter.reset();

		ShouldNotBeCalledHandler.callCounter.reset();
	}

	@Test
	void classLevelExceptionHandlersRethrowException() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(RethrowingTestCase.class)).build();
		EngineExecutionResults executionResults = executeTests(request);

		assertEquals(1, RethrowExceptionHandler.callCounter.getBeforeAllCalls(),
			"Exception should handled in @BeforeAll");
		assertEquals(1, RethrowExceptionHandler.callCounter.getAfterAllCalls(),
			"Exception should handled in @AfterAll");

		executionResults.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(RethrowingTestCase.class), started()), //
			event(container(RethrowingTestCase.class), finishedWithFailure(instanceOf(RuntimeException.class))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void testLevelExceptionHandlersRethrowException() {
		throwExceptionBeforeAll = false;
		throwExceptionAfterAll = false;
		LauncherDiscoveryRequest request = request().selectors(selectClass(RethrowingTestCase.class)).build();
		EngineExecutionResults executionResults = executeTests(request);

		assertEquals(1, RethrowExceptionHandler.callCounter.getBeforeEachCalls(),
			"Exception should be handled in @BeforeEach");
		assertEquals(1, RethrowExceptionHandler.callCounter.getAfterEachCalls(),
			"Exception should be handled in @AfterEach");

		executionResults.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(RethrowingTestCase.class), started()), //
			event(test("aTest"), started()), //
			event(test("aTest"), finishedWithFailure(instanceOf(RuntimeException.class))), //
			event(container(RethrowingTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void classLevelExceptionHandlersConvertException() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(ConvertingTestCase.class)).build();
		EngineExecutionResults executionResults = executeTests(request);

		assertEquals(1, ConvertExceptionHandler.callCounter.getBeforeAllCalls(),
			"Exception should handled in @BeforeAll");
		assertEquals(1, ConvertExceptionHandler.callCounter.getAfterAllCalls(),
			"Exception should handled in @AfterAll");

		executionResults.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(ConvertingTestCase.class), started()), //
			event(container(ConvertingTestCase.class), finishedWithFailure(instanceOf(IOException.class))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void testLevelExceptionHandlersConvertException() {
		throwExceptionBeforeAll = false;
		throwExceptionAfterAll = false;
		LauncherDiscoveryRequest request = request().selectors(selectClass(ConvertingTestCase.class)).build();
		EngineExecutionResults executionResults = executeTests(request);

		assertEquals(1, ConvertExceptionHandler.callCounter.getBeforeEachCalls(),
			"Exception should be handled in @BeforeEach");
		assertEquals(1, ConvertExceptionHandler.callCounter.getAfterEachCalls(),
			"Exception should be handled in @AfterEach");

		executionResults.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(ConvertingTestCase.class), started()), //
			event(test("aTest"), started()), //
			event(test("aTest"), finishedWithFailure(instanceOf(IOException.class))), //
			event(container(ConvertingTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void exceptionHandlersSwallowException() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(SwallowingTestCase.class)).build();
		EngineExecutionResults executionResults = executeTests(request);

		assertEquals(1, SwallowExceptionHandler.callCounter.getBeforeAllCalls(),
			"Exception should be handled in @BeforeAll");
		assertEquals(1, SwallowExceptionHandler.callCounter.getBeforeEachCalls(),
			"Exception should be handled in @BeforeEach");
		assertEquals(1, SwallowExceptionHandler.callCounter.getAfterEachCalls(),
			"Exception should be handled in @AfterEach");
		assertEquals(1, SwallowExceptionHandler.callCounter.getAfterAllCalls(),
			"Exception should be handled in @AfterAll");

		executionResults.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(SwallowingTestCase.class), started()), //
			event(test("aTest"), started()), //
			event(test("aTest"), finishedSuccessfully()), //
			event(container(SwallowingTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void perClassLifecycleMethodsAreHandled() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(PerClassLifecycleTestCase.class)).build();
		EngineExecutionResults executionResults = executeTests(request);
		assertEquals(2, SwallowExceptionHandler.callCounter.getBeforeAllCalls(),
			"Exception should be handled in @BeforeAll");
		assertEquals(1, SwallowExceptionHandler.callCounter.getBeforeEachCalls(),
			"Exception should be handled in @BeforeEach");
		assertEquals(1, SwallowExceptionHandler.callCounter.getAfterEachCalls(),
			"Exception should be handled in @AfterEach");
		assertEquals(2, SwallowExceptionHandler.callCounter.getAfterAllCalls(),
			"Exception should be handled in @AfterAll");

		executionResults.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(PerClassLifecycleTestCase.class), started()), //
			event(test("aTest"), started()), //
			event(test("aTest"), finishedSuccessfully()), //
			event(container(PerClassLifecycleTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void multipleHandlersAreCalledInOrder() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(MultipleHandlersTestCase.class)).build();
		EngineExecutionResults executionResults = executeTests(request);

		executionResults.allEvents().assertEventsMatchExactly( //
			event(engine(), started()), //
			event(container(MultipleHandlersTestCase.class), started()), //
			event(test("aTest"), started()), //
			event(test("aTest"), finishedSuccessfully()), //
			event(test("aTest2"), started()), //
			event(test("aTest2"), finishedSuccessfully()), //
			event(container(MultipleHandlersTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully())); //

		assertEquals(Arrays.asList(
			// BeforeAll chain (class level only)
			"RethrowExceptionBeforeAll", "SwallowExceptionBeforeAll",
			// BeforeEach chain for aTest (test + class level)
			"ConvertExceptionBeforeEach", "RethrowExceptionBeforeEach", "SwallowExceptionBeforeEach",
			// AfterEach chain for aTest  (test + class level)
			"ConvertExceptionAfterEach", "RethrowExceptionAfterEach", "SwallowExceptionAfterEach",
			// BeforeEach chain for aTest2 (class level only)
			"RethrowExceptionBeforeEach", "SwallowExceptionBeforeEach",
			// AfterEach chain for aTest2 (class level only)
			"RethrowExceptionAfterEach", "SwallowExceptionAfterEach",
			// AfterAll chain (class level only)
			"RethrowExceptionAfterAll", "SwallowExceptionAfterAll" //
		), handlerCalls, "Wrong order of handler calls");
	}

	@Test
	void unrecoverableExceptionsAreNotPropagatedInBeforeAll() {
		throwExceptionBeforeAll = true;
		throwExceptionBeforeEach = false;
		throwExceptionAfterEach = false;
		throwExceptionAfterAll = false;

		boolean unrecoverableExceptionThrown = executeThrowingOutOfMemoryException();
		assertTrue(unrecoverableExceptionThrown, "Unrecoverable Exception should be thrown");
		assertEquals(1, UnrecoverableExceptionHandler.callCounter.getBeforeAllCalls(),
			"Exception should be handled in @BeforeAll");
		assertEquals(0, ShouldNotBeCalledHandler.callCounter.getBeforeAllCalls(),
			"Exception should not propagate in @BeforeAll");
	}

	@Test
	void unrecoverableExceptionsAreNotPropagatedInBeforeEach() {
		throwExceptionBeforeAll = false;
		throwExceptionBeforeEach = true;
		throwExceptionAfterEach = false;
		throwExceptionAfterAll = false;

		boolean unrecoverableExceptionThrown = executeThrowingOutOfMemoryException();
		assertTrue(unrecoverableExceptionThrown, "Unrecoverable Exception should be thrown");
		assertEquals(1, UnrecoverableExceptionHandler.callCounter.getBeforeEachCalls(),
			"Exception should be handled in @BeforeEach");
		assertEquals(0, ShouldNotBeCalledHandler.callCounter.getBeforeEachCalls(),
			"Exception should not propagate in @BeforeEach");
	}

	@Test
	void unrecoverableExceptionsAreNotPropagatedInAfterEach() {
		throwExceptionBeforeAll = false;
		throwExceptionBeforeEach = false;
		throwExceptionAfterEach = true;
		throwExceptionAfterAll = false;

		boolean unrecoverableExceptionThrown = executeThrowingOutOfMemoryException();
		assertTrue(unrecoverableExceptionThrown, "Unrecoverable Exception should be thrown");
		assertEquals(1, UnrecoverableExceptionHandler.callCounter.getAfterEachCalls(),
			"Exception should be handled in @AfterEach");
		assertEquals(0, ShouldNotBeCalledHandler.callCounter.getAfterEachCalls(),
			"Exception should not propagate in @AfterEach");
	}

	@Test
	void unrecoverableExceptionsAreNotPropagatedInAfterAll() {
		throwExceptionBeforeAll = false;
		throwExceptionBeforeEach = false;
		throwExceptionAfterEach = false;
		throwExceptionAfterAll = true;

		boolean unrecoverableExceptionThrown = executeThrowingOutOfMemoryException();
		assertTrue(unrecoverableExceptionThrown, "Unrecoverable Exception should be thrown");
		assertEquals(1, UnrecoverableExceptionHandler.callCounter.getAfterAllCalls(),
			"Exception should be handled in @AfterAll");
		assertEquals(0, ShouldNotBeCalledHandler.callCounter.getAfterAllCalls(),
			"Exception should not propagate in @AfterAll");
	}

	private boolean executeThrowingOutOfMemoryException() {
		LauncherDiscoveryRequest request = request().selectors(
			selectClass(UnrecoverableExceptionTestCase.class)).build();
		try {
			executeTests(request);
		}
		catch (OutOfMemoryError expected) {
			return true;
		}
		return false;
	}

	// ------------------------------------------

	static class BaseTestCase {
		@BeforeAll
		static void throwBeforeAll() {
			if (throwExceptionBeforeAll) {
				throw new RuntimeException("BeforeAllEx");
			}
		}

		@BeforeEach
		void throwBeforeEach() {
			if (throwExceptionBeforeEach) {
				throw new RuntimeException("BeforeEachEx");
			}
		}

		@Test
		void aTest() {
		}

		@AfterEach
		void throwAfterEach() {
			if (throwExceptionAfterEach) {
				throw new RuntimeException("AfterEachEx");
			}
		}

		@AfterAll
		static void throwAfterAll() {
			if (throwExceptionAfterAll) {
				throw new RuntimeException("AfterAllEx");
			}
		}
	}

	@ExtendWith(RethrowExceptionHandler.class)
	static class RethrowingTestCase extends BaseTestCase {
	}

	@ExtendWith(ConvertExceptionHandler.class)
	static class ConvertingTestCase extends BaseTestCase {
	}

	@ExtendWith(SwallowExceptionHandler.class)
	static class SwallowingTestCase extends BaseTestCase {
	}

	@ExtendWith(ShouldNotBeCalledHandler.class)
	@ExtendWith(UnrecoverableExceptionHandler.class)
	static class UnrecoverableExceptionTestCase extends BaseTestCase {
	}

	@ExtendWith(ShouldNotBeCalledHandler.class)
	@ExtendWith(SwallowExceptionHandler.class)
	@ExtendWith(RethrowExceptionHandler.class)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	static class MultipleHandlersTestCase extends BaseTestCase {

		@Override
		@ExtendWith(ConvertExceptionHandler.class)
		@Order(1)
		@Test
		void aTest() {
		}

		@Order(2)
		@Test
		void aTest2() {
		}
	}

	@ExtendWith(SwallowExceptionHandler.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	static class PerClassLifecycleTestCase extends BaseTestCase {

		@BeforeAll
		void beforeAll() {
			throw new RuntimeException("nonStaticBeforeAllEx");
		}

		@AfterAll
		void afterAll() {
			throw new RuntimeException("nonStaticAfterAllEx");
		}
	}

	// ------------------------------------------

	static class RethrowExceptionHandler implements LifecycleMethodExecutionExceptionHandler {
		static HandlerCallCounter callCounter = new HandlerCallCounter();

		@Override
		public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementBeforeAllCalls();
			handlerCalls.add("RethrowExceptionBeforeAll");
			throw throwable;
		}

		@Override
		public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementBeforeEachCalls();
			handlerCalls.add("RethrowExceptionBeforeEach");
			throw throwable;
		}

		@Override
		public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementAfterEachCalls();
			handlerCalls.add("RethrowExceptionAfterEach");
			throw throwable;
		}

		@Override
		public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementAfterAllCalls();
			handlerCalls.add("RethrowExceptionAfterAll");
			throw throwable;
		}
	}

	static class SwallowExceptionHandler implements LifecycleMethodExecutionExceptionHandler {
		static HandlerCallCounter callCounter = new HandlerCallCounter();

		@Override
		public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
			callCounter.incrementBeforeAllCalls();
			handlerCalls.add("SwallowExceptionBeforeAll");
			// Do not rethrow
		}

		@Override
		public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
			callCounter.incrementBeforeEachCalls();
			handlerCalls.add("SwallowExceptionBeforeEach");
			// Do not rethrow
		}

		@Override
		public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
			callCounter.incrementAfterEachCalls();
			handlerCalls.add("SwallowExceptionAfterEach");
			// Do not rethrow
		}

		@Override
		public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
			callCounter.incrementAfterAllCalls();
			handlerCalls.add("SwallowExceptionAfterAll");
			// Do not rethrow
		}
	}

	static class ConvertExceptionHandler implements LifecycleMethodExecutionExceptionHandler {
		static HandlerCallCounter callCounter = new HandlerCallCounter();

		@Override
		public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementBeforeAllCalls();
			handlerCalls.add("ConvertExceptionBeforeAll");
			throw new IOException(throwable);
		}

		@Override
		public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementBeforeEachCalls();
			handlerCalls.add("ConvertExceptionBeforeEach");
			throw new IOException(throwable);
		}

		@Override
		public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementAfterEachCalls();
			handlerCalls.add("ConvertExceptionAfterEach");
			throw new IOException(throwable);
		}

		@Override
		public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementAfterAllCalls();
			handlerCalls.add("ConvertExceptionAfterAll");
			throw new IOException(throwable);
		}
	}

	static class UnrecoverableExceptionHandler implements LifecycleMethodExecutionExceptionHandler {
		static HandlerCallCounter callCounter = new HandlerCallCounter();

		@Override
		public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
			callCounter.incrementBeforeAllCalls();
			handlerCalls.add("UnrecoverableExceptionBeforeAll");
			throw new OutOfMemoryError();
		}

		@Override
		public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
			callCounter.incrementBeforeEachCalls();
			handlerCalls.add("UnrecoverableExceptionBeforeEach");
			throw new OutOfMemoryError();
		}

		@Override
		public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) {
			callCounter.incrementAfterEachCalls();
			handlerCalls.add("UnrecoverableExceptionAfterEach");
			throw new OutOfMemoryError();
		}

		@Override
		public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable) {
			callCounter.incrementAfterAllCalls();
			handlerCalls.add("UnrecoverableExceptionAfterAll");
			throw new OutOfMemoryError();
		}
	}

	static class ShouldNotBeCalledHandler implements LifecycleMethodExecutionExceptionHandler {
		static HandlerCallCounter callCounter = new HandlerCallCounter();

		@Override
		public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementBeforeAllCalls();
			handlerCalls.add("ShouldNotBeCalledBeforeAll");
			throw throwable;
		}

		@Override
		public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			ShouldNotBeCalledHandler.callCounter.incrementBeforeEachCalls();
			handlerCalls.add("ShouldNotBeCalledBeforeEach");
			throw throwable;
		}

		@Override
		public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementAfterEachCalls();
			handlerCalls.add("ShouldNotBeCalledAfterEach");
			throw throwable;
		}

		@Override
		public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable)
				throws Throwable {
			callCounter.incrementAfterAllCalls();
			handlerCalls.add("ShouldNotBeCalledAfterAll");
			throw throwable;
		}
	}
}
