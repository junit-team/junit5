/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static org.assertj.core.api.Assertions.allOf;
import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.engine.ExecutionEventConditions.*;
import static org.junit.gen5.engine.TestExecutionResultConditions.*;
import static org.junit.gen5.engine.discovery.MethodSelector.forMethod;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.io.IOException;

import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExceptionHandlerExtensionPoint;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.junit5.AbstractJUnit5TestEngineTests;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * Integration tests that verify support for {@link InstancePostProcessor}.
 */
class ExceptionHandlerExtensionPointTests extends AbstractJUnit5TestEngineTests {

	@Test
	void exceptionHandlerRethrowsException() {
		TestDiscoveryRequest request = request().select(forMethod(ATestCase.class, "testRethrow")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertTrue(RethrowException.handleExceptionCalled, "ExceptionHandler should have been called");

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
		TestDiscoveryRequest request = request().select(forMethod(ATestCase.class, "testSwallow")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertTrue(SwallowException.handleExceptionCalled, "ExceptionHandler should have been called");

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
		TestDiscoveryRequest request = request().select(forMethod(ATestCase.class, "testConvert")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertTrue(ConvertException.handleExceptionCalled, "ExceptionHandler should have been called");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(ATestCase.class), started()), //
			event(test("testConvert"), started()), //
			event(test("testConvert"), finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(container(ATestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	// -------------------------------------------------------------------

	private static class ATestCase {

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

	}

	private static class RethrowException implements ExceptionHandlerExtensionPoint {

		static boolean handleExceptionCalled = false;

		@Override
		public void handleException(TestExtensionContext context, Throwable throwable) throws Throwable {
			assertTrue(throwable instanceof IOException);
			handleExceptionCalled = true;

			throw throwable;
		}
	}

	private static class SwallowException implements ExceptionHandlerExtensionPoint {

		static boolean handleExceptionCalled = false;

		@Override
		public void handleException(TestExtensionContext context, Throwable throwable) throws Throwable {
			assertTrue(throwable instanceof IOException);
			handleExceptionCalled = true;
			//swallow exception by not rethrowing it
		}
	}

	private static class ConvertException implements ExceptionHandlerExtensionPoint {

		static boolean handleExceptionCalled = false;

		@Override
		public void handleException(TestExtensionContext context, Throwable throwable) throws Throwable {
			assertTrue(throwable instanceof RuntimeException);
			handleExceptionCalled = true;
			throw new IOException("checked");
		}
	}
}
