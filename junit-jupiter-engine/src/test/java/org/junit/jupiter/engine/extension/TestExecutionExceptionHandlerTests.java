/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.container;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.engine;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.isA;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests that verify support for {@link TestExecutionExceptionHandler}.
 *
 * @since 5.0
 */
class TestExecutionExceptionHandlerTests extends AbstractJupiterTestEngineTests {

	static List<String> handlerCalls = new ArrayList<>();

	@BeforeEach
	void resetStatics() {
		handlerCalls.clear();
		RethrowException.handleExceptionCalled = false;
		ConvertException.handleExceptionCalled = false;
		SwallowException.handleExceptionCalled = false;
		ShouldNotBeCalled.handleExceptionCalled = false;
	}

	@Test
	void exceptionHandlerRethrowsException() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(ATestCase.class, "testRethrow")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertTrue(RethrowException.handleExceptionCalled, "TestExecutionExceptionHandler should have been called");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ATestCase.class), started()), //
			event(test("testRethrow"), started()), //
			event(test("testRethrow"), finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(container(ATestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void exceptionHandlerSwallowsException() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(ATestCase.class, "testSwallow")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertTrue(SwallowException.handleExceptionCalled, "TestExecutionExceptionHandler should have been called");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ATestCase.class), started()), //
			event(test("testSwallow"), started()), //
			event(test("testSwallow"), finishedSuccessfully()), //
			event(container(ATestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void exceptionHandlerConvertsException() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(ATestCase.class, "testConvert")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertTrue(ConvertException.handleExceptionCalled, "TestExecutionExceptionHandler should have been called");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ATestCase.class), started()), //
			event(test("testConvert"), started()), //
			event(test("testConvert"), finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(container(ATestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void severalHandlersAreCalledInOrder() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(ATestCase.class, "testSeveral")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertTrue(ConvertException.handleExceptionCalled, "ConvertException should have been called");
		assertTrue(RethrowException.handleExceptionCalled, "RethrowException should have been called");
		assertTrue(SwallowException.handleExceptionCalled, "SwallowException should have been called");
		assertFalse(ShouldNotBeCalled.handleExceptionCalled, "ShouldNotBeCalled should not have been called");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ATestCase.class), started()), //
			event(test("testSeveral"), started()), //
			event(test("testSeveral"), finishedSuccessfully()), //
			event(container(ATestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertEquals(Arrays.asList("convert", "rethrow", "swallow"), handlerCalls);
	}

	// -------------------------------------------------------------------

	static class ATestCase {

		@Test
		@ExtendWith(RethrowException.class)
		void testRethrow() throws IOException {
			throw new IOException("checked");
		}

		@Test
		@ExtendWith(SwallowException.class)
		void testSwallow() throws IOException {
			throw new IOException("checked");
		}

		@Test
		@ExtendWith(ConvertException.class)
		void testConvert() {
			throw new RuntimeException("unchecked");
		}

		@Test
		@ExtendWith(ShouldNotBeCalled.class)
		@ExtendWith(SwallowException.class)
		@ExtendWith(RethrowException.class)
		@ExtendWith(ConvertException.class)
		void testSeveral() {
			throw new RuntimeException("unchecked");
		}
	}

	static class RethrowException implements TestExecutionExceptionHandler {

		static boolean handleExceptionCalled = false;

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			assertTrue(throwable instanceof IOException);
			handleExceptionCalled = true;
			handlerCalls.add("rethrow");

			throw throwable;
		}
	}

	static class SwallowException implements TestExecutionExceptionHandler {

		static boolean handleExceptionCalled = false;

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			assertTrue(throwable instanceof IOException);
			handleExceptionCalled = true;
			handlerCalls.add("swallow");
			//swallow exception by not rethrowing it
		}
	}

	static class ConvertException implements TestExecutionExceptionHandler {

		static boolean handleExceptionCalled = false;

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			assertTrue(throwable instanceof RuntimeException);
			handleExceptionCalled = true;
			handlerCalls.add("convert");
			throw new IOException("checked");
		}

	}

	static class ShouldNotBeCalled implements TestExecutionExceptionHandler {

		static boolean handleExceptionCalled = false;

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			handleExceptionCalled = true;
		}
	}

}
