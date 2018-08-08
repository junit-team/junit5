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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests that verify exception handling of {@code @BeforeAll} and {@code @AfterAll} methods in
 * {@link TestExecutionExceptionHandler}.
 */
class BeforeAndAfterAllExceptionHandlerTests extends AbstractJupiterTestEngineTests {

	static List<String> handlerCalls = new ArrayList<>();

	@BeforeEach
	void resetStatics() {
		handlerCalls.clear();
	}

	@Test
	void exceptionHandlerRethrowsException() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(RethrowTestCase.class, "test")).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(RethrowTestCase.class), started()), //
			event(container(RethrowTestCase.class), finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(engine(), finishedSuccessfully()));

		assertEquals(Arrays.asList("rethrowBeforeAll", "rethrowAfterAll", "rethrowAfterAll"), handlerCalls);
	}

	@Test
	void exceptionHandlerSwallowsException() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(SwallowTestCase.class, "test")).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(SwallowTestCase.class), started()), //
			event(test("test"), started()), //
			event(test("test"), finishedSuccessfully()), //
			event(container(SwallowTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertEquals(Arrays.asList("swallowBeforeAll", "swallowBeforeAll", "swallowTest", "swallowAfterAll", "swallowAfterAll"), handlerCalls);
	}

	@Test
	void exceptionHandlerConvertsException() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(ConvertTestCase.class, "test")).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ConvertTestCase.class), started()), //
			event(container(ConvertTestCase.class), finishedWithFailure(allOf(isA(RuntimeException.class), message("unchecked")))), //
			event(engine(), finishedSuccessfully()));

		assertEquals(Arrays.asList("convertBeforeAll", "convertAfterAll", "convertAfterAll"), handlerCalls);
	}

	@Test
	void severalHandlersAreCalledInOrder() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(SeveralTestCase.class, "test")).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(SeveralTestCase.class), started()), //
			event(test("test"), started()), //
			event(test("test"), finishedSuccessfully()), //
			event(container(SeveralTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertEquals(Arrays.asList( //
				"convertBeforeAll", "rethrowBeforeAll", "swallowBeforeAll", //
				"convertBeforeAll", "rethrowBeforeAll", "swallowBeforeAll", //
				"convertTest", "rethrowTest", "swallowTest", //
				"convertAfterAll", "rethrowAfterAll", "swallowAfterAll", //
				"convertAfterAll", "rethrowAfterAll", "swallowAfterAll"), //
				handlerCalls);
	}

	// -------------------------------------------------------------------

	static abstract class ATestCase {

		@BeforeAll
		static void beforeAll() throws IOException {
			throw new IOException("checked");
		}

		@BeforeAll
		static void beforeAll2() throws IOException {
			throw new IOException("checked");
		}

		@Test
		void test() throws IOException {
			throw new IOException("checked");
		}

		@AfterAll
		static void afterAll() throws IOException {
			throw new IOException("checked");
		}

		@AfterAll
		static void afterAll2() throws IOException {
			throw new IOException("checked");
		}
	}

	@ExtendWith(RethrowException.class)
	static class RethrowTestCase extends ATestCase {
	}

	@ExtendWith(SwallowException.class)
	static class SwallowTestCase extends ATestCase {
	}

	@ExtendWith(ConvertException.class)
	static class ConvertTestCase extends ATestCase {
	}

	@ExtendWith(ShouldNotBeCalled.class)
	@ExtendWith(SwallowException.class)
	@ExtendWith(RethrowException.class)
	@ExtendWith(ConvertException.class)
	static class SeveralTestCase extends ATestCase {
	}

	static class RethrowException implements TestExecutionExceptionHandler {

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("rethrowTest");
			throw throwable;
		}

		@Override
		public void handleExceptionInBeforeAllMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("rethrowBeforeAll");
			throw throwable;
		}

		@Override
		public void handleExceptionInAfterAllMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("rethrowAfterAll");
			throw throwable;
		}
	}

	static class SwallowException implements TestExecutionExceptionHandler {
		// swallow exception by not rethrowing it

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("swallowTest");
		}

		@Override
		public void handleExceptionInBeforeAllMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("swallowBeforeAll");
		}

		@Override
		public void handleExceptionInAfterAllMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("swallowAfterAll");
		}
	}

	static class ConvertException implements TestExecutionExceptionHandler {

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("convertTest");
			throw new RuntimeException("unchecked");
		}

		@Override
		public void handleExceptionInBeforeAllMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("convertBeforeAll");
			throw new RuntimeException("unchecked");
		}

		@Override
		public void handleExceptionInAfterAllMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("convertAfterAll");
			throw new RuntimeException("unchecked");
		}
	}

	static class ShouldNotBeCalled implements TestExecutionExceptionHandler {

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("shouldNotBeCalledTest");
		}

		@Override
		public void handleExceptionInBeforeAllMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("shouldNotBeCalledBeforeAll");
		}

		@Override
		public void handleExceptionInAfterAllMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("shouldNotBeCalledAfterAll");
		}
	}
}
