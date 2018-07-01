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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

/**
 * Integration tests that verify support for {@link TestInstanceFactory}.
 *
 * @since 5.3
 */
class TestInstanceFactoryTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
	}

	@Test
	void multipleFactoriesRegisteredOnSingleTestClass() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(MultipleFactoriesRegisteredTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests aborted");
	}

	@Test
	void bogusTestInstanceFactory() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(BogusTestInstanceFactoryTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests aborted");
	}

	@Test
	void instanceFactoriesInNestedClassHierarchy() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(OuterTestCase.class);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		// @formatter:off
		assertThat(callSequence).containsExactly(

			// OuterTestCase
			"FooInstanceFactory instantiated: OuterTestCase",
				"beforeOuterMethod",
					"testOuter",

			// InnerTestCase
			"FooInstanceFactory instantiated: OuterTestCase",
				"BarInstanceFactory instantiated: InnerTestCase",
					"beforeOuterMethod",
						"beforeInnerMethod",
							"testInner"

		);
		// @formatter:on
	}

	@Test
	void instanceFactoryRegisteredAsLambdaExpression() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(LambdaFactoryTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		// @formatter:off
		assertThat(callSequence).containsExactly(
			"beforeEach: lambda",
				"test: lambda"
		);
		// @formatter:on
	}

	@Test
	void instanceFactoryUsingPerClassTestInstanceLifecycle() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(PerClassLifecycleTestCase.class);

		assertEquals(1, PerClassLifecycleTestCase.counter.get());

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		// @formatter:off
		assertThat(callSequence).containsExactly(

			"FooInstanceFactory instantiated: PerClassLifecycleTestCase",
				"@BeforeAll",
					"@BeforeEach",
						"test1",
					"@BeforeEach",
						"test2",
				"@AfterAll"

		);
		// @formatter:on
	}

	// -------------------------------------------------------------------------

	@ExtendWith({ FooInstanceFactory.class, BarInstanceFactory.class })
	static class MultipleFactoriesRegisteredTestCase {

		@Test
		void testShouldNotBeCalled() {
			callSequence.add("testShouldNotBeCalled");
		}

	}

	@ExtendWith(BogusTestInstanceFactory.class)
	static class BogusTestInstanceFactoryTestCase {

		@Test
		void testShouldNotBeCalled() {
			callSequence.add("testShouldNotBeCalled");
		}

	}

	@ExtendWith(FooInstanceFactory.class)
	static class OuterTestCase {

		@BeforeEach
		void beforeOuterMethod() {
			callSequence.add("beforeOuterMethod");
		}

		@Test
		void testOuter() {
			callSequence.add("testOuter");
		}

		@Nested
		@ExtendWith(BarInstanceFactory.class)
		class InnerTestCase {

			@BeforeEach
			void beforeInnerMethod() {
				callSequence.add("beforeInnerMethod");
			}

			@Test
			void testInner() {
				callSequence.add("testInner");
			}
		}

	}

	static class LambdaFactoryTestCase {

		private final String text;

		@RegisterExtension
		static final TestInstanceFactory factory = (__, ___) -> new LambdaFactoryTestCase("lambda");

		LambdaFactoryTestCase(String text) {
			this.text = text;
		}

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeEach: " + this.text);
		}

		@Test
		void test() {
			callSequence.add("test: " + this.text);
		}

	}

	@ExtendWith(FooInstanceFactory.class)
	@TestInstance(PER_CLASS)
	static class PerClassLifecycleTestCase {

		static final AtomicInteger counter = new AtomicInteger();

		PerClassLifecycleTestCase() {
			counter.incrementAndGet();
		}

		@BeforeAll
		void beforeAll() {
			callSequence.add("@BeforeAll");
		}

		@BeforeEach
		void beforeEach() {
			callSequence.add("@BeforeEach");
		}

		@Test
		void test1() {
			callSequence.add("test1");
		}

		@Test
		void test2() {
			callSequence.add("test2");
		}

		@AfterAll
		void afterAll() {
			callSequence.add("@AfterAll");
		}

	}

	private static class FooInstanceFactory implements TestInstanceFactory {

		@Override
		public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) {
			Class<?> testClass = factoryContext.getTestClass();
			instantiated(getClass(), testClass);
			return ReflectionUtils.newInstance(testClass);
		}
	}

	private static class BarInstanceFactory implements TestInstanceFactory {

		@Override
		public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) {
			Class<?> testClass = factoryContext.getTestClass();
			Object outerInstance = factoryContext.getOuterInstance().get();
			instantiated(getClass(), testClass);
			return ReflectionUtils.newInstance(testClass, outerInstance);
		}
	}

	/**
	 * {@link TestInstanceFactory} that returns an object of a type that does
	 * not match the supplied test class.
	 */
	private static class BogusTestInstanceFactory implements TestInstanceFactory {

		@Override
		public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) {
			return "bogus";
		}
	}

	private static boolean instantiated(Class<? extends TestInstanceFactory> factoryClass, Class<?> testClass) {
		return callSequence.add(factoryClass.getSimpleName() + " instantiated: " + testClass.getSimpleName());
	}

}
