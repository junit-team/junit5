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
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

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
	void instanceFactoriesInNestedClasses() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(OuterTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		// @formatter:off
		assertThat(callSequence).containsExactly(

			// OuterTestCase
			"fooInstanceFactoryInstantiated:OuterTestCase",
				"beforeOuterMethod",
					"testOuter",

			// InnerTestCase

			"fooInstanceFactoryInstantiated:OuterTestCase",
			"barInstanceFactoryInstantiatedNested:InnerTestCase",
					"beforeOuterMethod",
						"beforeInnerMethod",
							"testInner"
		);
		// @formatter:on
	}

	@Test
	void invalidFactoryRegistration() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(InvalidTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests aborted");
	}

	@Test
	void instanceFactoriesInPerClassTests() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(PerClassTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		// @formatter:off
		assertThat(callSequence).containsExactly(

			"perClassInstanceFactoryInstantiated:PerClassTestCase",
				"initCounter",
					"incrementCounter",
						"aTest",
					"incrementCounter",
						"bTest",
				"checkCounter"

		);
		// @formatter:on
	}

	// -------------------------------------------------------------------

	@ExtendWith(PerClassInstanceFactory.class)
	@TestInstance(PER_CLASS)
	static class PerClassTestCase {

		int counter = -1;

		@BeforeAll
		void initCounter() {
			callSequence.add("initCounter");
			counter = 0;
		}

		@BeforeEach
		void incrementCounter() {
			callSequence.add("incrementCounter");
			counter += 1;
		}

		@Test
		void aTest() {
			callSequence.add("aTest");
			assertEquals(counter, 1);
		}

		@Test
		void bTest() {
			callSequence.add("bTest");
			assertEquals(counter, 2);
		}

		@AfterAll
		void checkCounter() {
			assertEquals(counter, 2);
			callSequence.add("checkCounter");
		}

	}

	static class PerClassInstanceFactory implements TestInstanceFactory {

		@Override
		public Object instantiateTestClass(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
				throws TestInstantiationException {
			Class<?> testClass = factoryContext.getTestClass();
			callSequence.add("perClassInstanceFactoryInstantiated:" + testClass.getSimpleName());
			try {
				return testClass.getDeclaredConstructor().newInstance();
			}
			catch (Exception ex) {
				throw new TestInstantiationException("Failed to invoke constructor", ex);
			}
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

	@ExtendWith({ FooInstanceFactory.class, BarInstanceFactory.class })
	static class InvalidTestCase {

		@Test
		void testShouldNotBeCalled() {
			callSequence.add("testShouldNotBeCalled");
		}

	}

	static class FooInstanceFactory implements TestInstanceFactory {

		@Override
		public Object instantiateTestClass(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
				throws TestInstantiationException {
			Class<?> testClass = factoryContext.getTestClass();
			callSequence.add("fooInstanceFactoryInstantiated:" + testClass.getSimpleName());
			try {
				return testClass.getDeclaredConstructor().newInstance();
			}
			catch (Throwable ex) {
				throw new TestInstantiationException("Failed to invoke constructor", ex);
			}
		}

	}

	static class BarInstanceFactory implements TestInstanceFactory {

		@Override
		public Object instantiateTestClass(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
				throws TestInstantiationException {
			Class<?> testClass = factoryContext.getTestClass();
			Object outerInstance = factoryContext.getOuterInstance().orElseThrow(NoSuchElementException::new);
			callSequence.add("barInstanceFactoryInstantiatedNested:" + testClass.getSimpleName());
			try {
				return testClass.getDeclaredConstructor(outerInstance.getClass()).newInstance(outerInstance);
			}
			catch (Throwable ex) {
				throw new TestInstantiationException("Failed to invoke constructor", ex);
			}
		}
	}

}
