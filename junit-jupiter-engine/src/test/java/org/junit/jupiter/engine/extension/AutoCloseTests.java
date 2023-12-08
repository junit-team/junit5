/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Events;

/**
 * Integration tests for the behavior of the {@link AutoCloseExtension} to release resources after test execution.
 *
 * @since 5.11
 */
class AutoCloseTests extends AbstractJupiterTestEngineTests {

	private static final List<String> recorder = new ArrayList<>();

	@BeforeEach
	void resetRecorder() {
		recorder.clear();
	}

	@Test
	void fieldsAreProperlyClosed() {
		EngineExecutionResults engineExecutionResults = executeTestsForClass(AutoCloseTestCase.class);

		Events tests = engineExecutionResults.testEvents();
		tests.assertStatistics(stats -> stats.succeeded(2));
		// @formatter:off
		assertEquals(asList(
				"afterEach-close()", "afterEach-run()",
				"afterEach-close()", "afterEach-run()",
					"afterAll-close()"), recorder);
		// @formatter:onf
	}

	@Test
	void noCloseMethod() {
		String msg = "@AutoClose: Cannot resolve the destroy method close() at AutoCloseNoCloseMethodFailingTestCase.resource: String";

		Events tests = executeTestsForClass(AutoCloseNoCloseMethodFailingTestCase.class).testEvents();
		assertFailingWithMessage(tests, msg);
	}

	@Test
	void noShutdownMethod() {
		String msg = "@AutoClose: Cannot resolve the destroy method shutdown() at AutoCloseNoShutdownMethodFailingTestCase.resource: String";

		Events tests = executeTestsForClass(AutoCloseNoShutdownMethodFailingTestCase.class).testEvents();
		assertFailingWithMessage(tests, msg);
	}

	@Test
	void namespace() {
		assertEquals(Namespace.create(AutoClose.class), AutoCloseExtension.NAMESPACE);
	}

	@Test
	void spyPermitsOnlyASingleAction() {
		AutoCloseSpy spy = new AutoCloseSpy("");

		spy.close();

		assertThrows(IllegalStateException.class, spy::close);
		assertThrows(IllegalStateException.class, spy::run);
		assertEquals(asList("close()"), recorder);
	}

	private static void assertFailingWithMessage(Events testEvent, String msg) {
		testEvent.assertStatistics(stats -> stats.failed(1)).assertThatEvents().haveExactly(1,
			finishedWithFailure(cause(message(actual -> actual.contains(msg)))));
	}

	static class AutoCloseTestCase {

		private static @AutoClose AutoCloseable staticClosable;
		private static @AutoClose AutoCloseable nullStatic;

		private final @AutoClose AutoCloseable closable = new AutoCloseSpy("afterEach-");
		private final @AutoClose("run") Runnable runnable = new AutoCloseSpy("afterEach-");
		private @AutoClose AutoCloseable nullField;

		@Test
		void justPass() {
			assertFields();
		}

		@Test
		void anotherPass() {
			assertFields();
		}

		private void assertFields() {
			assertNotNull(staticClosable);
			assertNull(nullStatic);

			assertNotNull(closable);
			assertNotNull(runnable);
			assertNull(nullField);
		}

		@BeforeAll
		static void setup() {
			staticClosable = new AutoCloseSpy("afterAll-");
		}

	}

	static class AutoCloseNoCloseMethodFailingTestCase {

		@AutoClose
		private final String resource = "nothing to close()";

		@Test
		void alwaysPass() {
			assertNotNull(resource);
		}

	}

	static class AutoCloseNoShutdownMethodFailingTestCase {

		@AutoClose("shutdown")
		private final String resource = "nothing to shutdown()";

		@Test
		void alwaysPass() {
			assertNotNull(resource);
		}

	}

	static class AutoCloseSpy implements AutoCloseable, Runnable {

		private final String prefix;
		private String invokedMethod = "";

		public AutoCloseSpy(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public void run() {
			checkIfAlreadyInvoked();
			recordInvocation("run()");
		}

		@Override
		public void close() {
			checkIfAlreadyInvoked();
			recordInvocation("close()");
		}

		private void checkIfAlreadyInvoked() {
			if (!invokedMethod.isEmpty())
				throw new IllegalStateException();
		}

		private void recordInvocation(String methodName) {
			invokedMethod = methodName;
			recorder.add(prefix + methodName);
		}

	}

}
