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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_OUT;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.suppressed;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.PreInterruptCallback;
import org.junit.jupiter.api.extension.PreInterruptContext;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.Constants;
import org.junit.platform.testkit.engine.Events;

/**
 * @since 5.12
 */
@Isolated
class PreInterruptCallbackTests extends AbstractJupiterTestEngineTests {
	private static final String TC = "test";
	private static final String TIMEOUT_ERROR_MSG = TC + "() timed out after 1 microsecond";
	private static final String DEFAULT_ENABLE_PROPERTY = Constants.EXTENSIONS_TIMEOUT_THREAD_DUMP_ENABLED_PROPERTY_NAME;
	private static final AtomicBoolean interruptedTest = new AtomicBoolean();
	private static final CompletableFuture<Void> testThreadExecutionDone = new CompletableFuture<>();
	private static final AtomicReference<Thread> interruptedTestThread = new AtomicReference<>();
	private static final AtomicBoolean interruptCallbackShallThrowException = new AtomicBoolean();
	private static final AtomicReference<PreInterruptContext> calledPreInterruptContext = new AtomicReference<>();

	@BeforeEach
	void setUp() {
		interruptedTest.set(false);
		interruptCallbackShallThrowException.set(false);
		calledPreInterruptContext.set(null);
	}

	@AfterEach
	void tearDown() {
		calledPreInterruptContext.set(null);
		interruptedTestThread.set(null);
	}

	@Test
	@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
	@ResourceLock(value = SYSTEM_OUT, mode = READ_WRITE)
	void testCaseWithDefaultInterruptCallbackEnabled() {
		String orgValue = System.getProperty(DEFAULT_ENABLE_PROPERTY);
		System.setProperty(DEFAULT_ENABLE_PROPERTY, Boolean.TRUE.toString());
		PrintStream orgOutStream = System.out;
		Events tests;
		String output;
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			PrintStream outStream = new PrintStream(buffer);
			System.setOut(outStream);
			tests = executeTestsForClass(DefaultPreInterruptCallbackTimeoutOnMethodTestCase.class).testEvents();
			output = buffer.toString(StandardCharsets.UTF_8);
		}
		finally {
			System.setOut(orgOutStream);
			if (orgValue != null) {
				System.setProperty(DEFAULT_ENABLE_PROPERTY, orgValue);
			}
			else {
				System.clearProperty(DEFAULT_ENABLE_PROPERTY);
			}
		}

		assertTestHasTimedOut(tests);
		assertTrue(interruptedTest.get());
		Thread thread = Thread.currentThread();
		assertTrue(
			output.contains("Thread \"" + thread.getName() + "\" #" + thread.threadId() + " will be interrupted."),
			output);
		assertTrue(output.contains("java.lang.Thread.sleep"), output);
		assertTrue(output.contains(
			"org.junit.jupiter.engine.extension.PreInterruptCallbackTests$DefaultPreInterruptCallbackTimeoutOnMethodTestCase.test(PreInterruptCallbackTests.java"),
			output);

		assertTrue(output.contains("junit-jupiter-timeout-watcher"), output);
		assertTrue(
			output.contains("org.junit.jupiter.engine.extension.PreInterruptThreadDumpPrinter.beforeThreadInterrupt"),
			output);
	}

	@Test
	void testCaseWithNoInterruptCallbackEnabled() {
		Events tests = executeTestsForClass(DefaultPreInterruptCallbackTimeoutOnMethodTestCase.class).testEvents();
		assertTestHasTimedOut(tests);
		assertTrue(interruptedTest.get());
	}

	@Test
	void testCaseWithDeclaredInterruptCallbackEnabled() {
		Events tests = executeTestsForClass(DefaultPreInterruptCallbackWithExplicitCallbackTestCase.class).testEvents();
		assertTestHasTimedOut(tests);
		assertTrue(interruptedTest.get());
		PreInterruptContext preInterruptContext = calledPreInterruptContext.get();
		assertNotNull(preInterruptContext);
		assertNotNull(preInterruptContext.getThreadToInterrupt());
		assertEquals(preInterruptContext.getThreadToInterrupt(), interruptedTestThread.get());
		assertNotNull(preInterruptContext.getExtensionContext());
	}

	@Test
	void testCaseWithDeclaredInterruptCallbackEnabledWithSeparateThread() throws Exception {
		Events tests = executeTestsForClass(
			DefaultPreInterruptCallbackWithExplicitCallbackWithSeparateThreadTestCase.class).testEvents();
		assertOneFailedTest(tests);
		tests.failed().assertEventsMatchExactly(
			event(test(TC), finishedWithFailure(instanceOf(TimeoutException.class))));

		//Wait until the real test thread was interrupted due to executor.shutdown(), otherwise the asserts below will be flaky.
		testThreadExecutionDone.get(1, TimeUnit.SECONDS);

		assertTrue(interruptedTest.get());
		PreInterruptContext preInterruptContext = calledPreInterruptContext.get();
		assertNotNull(preInterruptContext);
		assertNotNull(preInterruptContext.getThreadToInterrupt());
		assertEquals(preInterruptContext.getThreadToInterrupt(), interruptedTestThread.get());
		assertNotNull(preInterruptContext.getExtensionContext());
	}

	@Test
	void testCaseWithDeclaredInterruptCallbackThrowsException() {
		interruptCallbackShallThrowException.set(true);
		Events tests = executeTestsForClass(DefaultPreInterruptCallbackWithExplicitCallbackTestCase.class).testEvents();
		tests.failed().assertEventsMatchExactly(event(test(TC),
			finishedWithFailure(instanceOf(TimeoutException.class), message(TIMEOUT_ERROR_MSG),
				suppressed(0, instanceOf(InterruptedException.class)),
				suppressed(1, instanceOf(IllegalStateException.class)))));
		assertTrue(interruptedTest.get());
		PreInterruptContext preInterruptContext = calledPreInterruptContext.get();
		assertNotNull(preInterruptContext);
		assertNotNull(preInterruptContext.getThreadToInterrupt());
		assertEquals(preInterruptContext.getThreadToInterrupt(), interruptedTestThread.get());
		assertNotNull(preInterruptContext.getExtensionContext());
	}

	private static void assertTestHasTimedOut(Events tests) {
		assertOneFailedTest(tests);
		tests.failed().assertEventsMatchExactly(
			event(test(TC), finishedWithFailure(instanceOf(TimeoutException.class), message(TIMEOUT_ERROR_MSG), //
				suppressed(0, instanceOf(InterruptedException.class))//
			)));
	}

	private static void assertOneFailedTest(Events tests) {
		tests.assertStatistics(stats -> stats.started(1).succeeded(0).failed(1));
	}

	static class TestPreInterruptCallback implements PreInterruptCallback {

		@Override
		public void beforeThreadInterrupt(PreInterruptContext preInterruptContext) {
			calledPreInterruptContext.set(preInterruptContext);
			if (interruptCallbackShallThrowException.get()) {
				throw new IllegalStateException("Test-Ex");
			}
		}
	}

	static class DefaultPreInterruptCallbackTimeoutOnMethodTestCase {
		@Test
		@Timeout(value = 1, unit = TimeUnit.MICROSECONDS)
		void test() throws InterruptedException {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException ex) {
				interruptedTest.set(true);
				interruptedTestThread.set(Thread.currentThread());
				throw ex;
			}
		}
	}

	@ExtendWith(TestPreInterruptCallback.class)
	static class DefaultPreInterruptCallbackWithExplicitCallbackTestCase {
		@Test
		@Timeout(value = 1, unit = TimeUnit.MICROSECONDS)
		void test() throws InterruptedException {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException ex) {
				interruptedTest.set(true);
				interruptedTestThread.set(Thread.currentThread());
				throw ex;
			}
		}
	}

	@ExtendWith(TestPreInterruptCallback.class)
	static class DefaultPreInterruptCallbackWithExplicitCallbackWithSeparateThreadTestCase {
		@Test
		@Timeout(value = 200, unit = TimeUnit.MILLISECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
		void test() throws InterruptedException {
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException ex) {
				interruptedTest.set(true);
				interruptedTestThread.set(Thread.currentThread());
				throw ex;
			}
			finally {
				testThreadExecutionDone.complete(null);
			}
		}
	}
}
