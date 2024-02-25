/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.testkit.engine.EngineExecutionResults;

/**
 * @since 5.9
 */
public class ExecutableInvokerIntegrationTests extends AbstractJupiterTestEngineTests {

	@Test
	void invokeConstructorViaExtensionContext() {
		EngineExecutionResults results = executeTestsForClass(ExecuteConstructorTwiceTestCase.class);

		assertEquals(1, results.testEvents().succeeded().count());
		assertEquals(2, ExecuteConstructorTwiceTestCase.constructorInvocations);
	}

	@Test
	void invokeMethodViaExtensionContext() {
		EngineExecutionResults results = executeTestsForClass(ExecuteTestsTwiceTestCase.class);

		assertEquals(1, results.testEvents().succeeded().count());
		assertEquals(2, ExecuteTestsTwiceTestCase.testInvocations);
	}

	@ExtendWith(ExecuteTestsTwiceExtension.class)
	static class ExecuteTestsTwiceTestCase {

		static int testInvocations = 0;

		@Test
		void testWithResolvedParameter(TestInfo testInfo) {
			assertNotNull(testInfo);
			testInvocations++;
		}

	}

	@ExtendWith(ExecuteConstructorTwiceExtension.class)
	static class ExecuteConstructorTwiceTestCase {

		static int constructorInvocations = 0;

		public ExecuteConstructorTwiceTestCase(TestInfo testInfo) {
			assertNotNull(testInfo);
			constructorInvocations++;
		}

		@Test
		void test() {

		}

	}

	static class ExecuteTestsTwiceExtension implements AfterTestExecutionCallback {

		@Override
		public void afterTestExecution(ExtensionContext context) {
			context.getExecutableInvoker() //
					.invoke(context.getRequiredTestMethod(), context.getRequiredTestInstance());
		}

	}

	static class ExecuteConstructorTwiceExtension implements BeforeAllCallback {

		@Override
		public void beforeAll(ExtensionContext context) throws Exception {
			context.getExecutableInvoker() //
					.invoke(context.getRequiredTestClass().getConstructor(TestInfo.class));
		}

	}

}
