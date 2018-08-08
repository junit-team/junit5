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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests that verify exception handling of {@code @BeforeEach} and {@code @AfterEach} methods in
 * {@link TestExecutionExceptionHandler}.
 */
class BeforeAndAfterEachExceptionHandlerTests extends AbstractJupiterTestEngineTests {

	static List<String> handlerCalls = new ArrayList<>();

	@BeforeEach
	void resetStatics() {
		handlerCalls.clear();
	}

	@Test
	void exceptionHandlerRethrowsException() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(ATestCase.class, "testRethrow")).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ATestCase.class), started()), //
			event(test("testRethrow"), started()), //
			event(test("testRethrow"), finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(container(ATestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertEquals(Arrays.asList("rethrowBeforeEach", "rethrowAfterEach", "rethrowAfterEach"), handlerCalls);
	}

	@Test
	void exceptionHandlerSwallowsException() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(ATestCase.class, "testSwallow")).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ATestCase.class), started()), //
			event(test("testSwallow"), started()), //
			event(test("testSwallow"), finishedSuccessfully()), //
			event(container(ATestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertEquals(Arrays.asList( //
			"swallowBeforeEach", "swallowBeforeEach", "swallowTest", "swallowAfterEach", "swallowAfterEach"), //
			handlerCalls);
	}

	@Test
	void exceptionHandlerConvertsException() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(ATestCase.class, "testConvert")).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ATestCase.class), started()), //
			event(test("testConvert"), started()), //
			event(test("testConvert"), finishedWithFailure(allOf(isA(RuntimeException.class), message("unchecked")))), //
			event(container(ATestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertEquals(Arrays.asList("convertBeforeEach", "convertAfterEach", "convertAfterEach"), handlerCalls);
	}

	@Test
	void severalHandlersAreCalledInOrder() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(ATestCase.class, "testSeveral")).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ATestCase.class), started()), //
			event(test("testSeveral"), started()), //
			event(test("testSeveral"), finishedSuccessfully()), //
			event(container(ATestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));

		assertEquals(Arrays.asList( //
			"convertBeforeEach", "rethrowBeforeEach", "swallowBeforeEach", //
			"convertBeforeEach", "rethrowBeforeEach", "swallowBeforeEach", //
			"convertTest", "rethrowTest", "swallowTest", //
			"convertAfterEach", "rethrowAfterEach", "swallowAfterEach", //
			"convertAfterEach", "rethrowAfterEach", "swallowAfterEach"), //
			handlerCalls);
	}

	// -------------------------------------------------------------------

	static class ATestCase {

		@BeforeEach
		void beforeEach() throws IOException {
			throw new IOException("checked");
		}

		@BeforeEach
		void beforeEach2() throws IOException {
			throw new IOException("checked");
		}

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
		void testConvert() throws IOException {
			throw new IOException("checked");
		}

		@Test
		@ExtendWith(ShouldNotBeCalled.class)
		@ExtendWith(SwallowException.class)
		@ExtendWith(RethrowException.class)
		@ExtendWith(ConvertException.class)
		void testSeveral() throws IOException {
			throw new IOException("checked");
		}

		@AfterEach
		void afterEach() throws IOException {
			throw new IOException("checked");
		}

		@AfterEach
		void afterEach2() throws IOException {
			throw new IOException("checked");
		}
	}

	static class RethrowException implements TestExecutionExceptionHandler {

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("rethrowTest");
			throw throwable;
		}

		@Override
		public void handleExceptionInBeforeEachMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("rethrowBeforeEach");
			throw throwable;
		}

		@Override
		public void handleExceptionInAfterEachMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("rethrowAfterEach");
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
		public void handleExceptionInBeforeEachMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("swallowBeforeEach");
		}

		@Override
		public void handleExceptionInAfterEachMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("swallowAfterEach");
		}
	}

	static class ConvertException implements TestExecutionExceptionHandler {

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("convertTest");
			throw new RuntimeException("unchecked");
		}

		@Override
		public void handleExceptionInBeforeEachMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("convertBeforeEach");
			throw new RuntimeException("unchecked");
		}

		@Override
		public void handleExceptionInAfterEachMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("convertAfterEach");
			throw new RuntimeException("unchecked");
		}
	}

	static class ShouldNotBeCalled implements TestExecutionExceptionHandler {

		@Override
		public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("shouldNotBeCalledTest");
		}

		@Override
		public void handleExceptionInBeforeEachMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("shouldNotBeCalledBeforeEach");
		}

		@Override
		public void handleExceptionInAfterEachMethod(ExtensionContext context, Throwable throwable) throws Throwable {
			handlerCalls.add("shouldNotBeCalledAfterEach");
		}
	}
}
