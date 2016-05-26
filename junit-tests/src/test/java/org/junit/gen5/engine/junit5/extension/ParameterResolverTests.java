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
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertNotNull;
import static org.junit.gen5.api.Assertions.assertNull;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.engine.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.gen5.engine.ExecutionEventConditions.event;
import static org.junit.gen5.engine.ExecutionEventConditions.finishedWithFailure;
import static org.junit.gen5.engine.ExecutionEventConditions.test;
import static org.junit.gen5.engine.TestExecutionResultConditions.isA;
import static org.junit.gen5.engine.TestExecutionResultConditions.message;

import java.util.Arrays;
import java.util.function.Predicate;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.DisplayName;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;
import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.api.extension.ParameterResolver;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.junit5.AbstractJUnit5TestEngineTests;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomAnnotation;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomAnnotationParameterResolver;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomType;
import org.junit.gen5.engine.junit5.execution.injection.sample.CustomTypeParameterResolver;
import org.junit.gen5.engine.junit5.execution.injection.sample.NullIntegerParameterResolver;
import org.junit.gen5.engine.junit5.execution.injection.sample.NumberParameterResolver;
import org.junit.gen5.engine.junit5.execution.injection.sample.PrimitiveArrayParameterResolver;
import org.junit.gen5.engine.junit5.execution.injection.sample.PrimitiveIntegerParameterResolver;

/**
 * Integration tests that verify support for {@link ParameterResolver}
 * extensions in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
class ParameterResolverTests extends AbstractJUnit5TestEngineTests {

	@Test
	void constructorInjection() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(ConstructorInjectionTestCase.class);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(MethodInjectionTestCase.class);

		assertEquals(7, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(6, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForNullValuedMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(NullMethodInjectionTestCase.class);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		Predicate<String> expectations = s ->
				s.contains("NullIntegerParameterResolver") &&
				s.contains("resolved a null value for parameter") &&
				s.contains("but a primitive of type [int] is required");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(),
			event(
				test("injectPrimitive"),
				finishedWithFailure(allOf(isA(ParameterResolutionException.class), message(expectations)))
			));
		// @formatter:on
	}

	@Test
	void executeTestsForPrimitiveIntegerMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(PrimitiveIntegerMethodInjectionTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForPrimitiveArrayMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(PrimitiveArrayMethodInjectionTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForPotentiallyIncompatibleTypeMethodInjectionCases() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(
			PotentiallyIncompatibleTypeMethodInjectionTestCase.class);

		assertEquals(3, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		Predicate<String> expectations = s ->
				s.contains("NumberParameterResolver") &&
				s.contains("resolved a value of type [java.lang.Integer]") &&
				s.contains("but a value assignment compatible with [java.lang.Double] is required");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(),
			event(
				test("doubleParameterInjection"),
				finishedWithFailure(allOf(isA(ParameterResolutionException.class), message(expectations)
			))));
		// @formatter:on
	}

	@Test
	void executeTestsForMethodInjectionInBeforeAndAfterEachMethods() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(BeforeAndAfterMethodInjectionTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForMethodInjectionInBeforeAndAfterAllMethods() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(BeforeAndAfterAllMethodInjectionTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestsForMethodWithExtendWithAnnotation() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(ExtendWithOnMethodTestCase.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	// -------------------------------------------------------------------

	@ExtendWith(CustomTypeParameterResolver.class)
	private static class ConstructorInjectionTestCase {

		private final TestInfo outerTestInfo;
		private final CustomType outerCustomType;

		@SuppressWarnings("unused")
		ConstructorInjectionTestCase(TestInfo testInfo, CustomType customType) {
			this.outerTestInfo = testInfo;
			this.outerCustomType = customType;
		}

		@Test
		void test() {
			assertNotNull(this.outerTestInfo);
			assertNotNull(this.outerCustomType);
		}

		@Nested
		class NestedTestCase {

			private final TestInfo innerTestInfo;
			private final CustomType innerCustomType;

			@SuppressWarnings("unused")
			NestedTestCase(TestInfo testInfo, CustomType customType) {
				this.innerTestInfo = testInfo;
				this.innerCustomType = customType;
			}

			@Test
			void test() {
				assertNotNull(outerTestInfo);
				assertNotNull(outerCustomType);
				assertNotNull(this.innerTestInfo);
				assertNotNull(this.innerCustomType);
			}
		}
	}

	@ExtendWith({ CustomTypeParameterResolver.class, CustomAnnotationParameterResolver.class })
	private static class MethodInjectionTestCase {

		@Test
		void parameterInjectionOfTestInfo(TestInfo testInfo) {
			assertNotNull(testInfo);
		}

		@Test
		void parameterInjectionWithCompetingResolversFail(@CustomAnnotation CustomType customType) {
			// should fail
		}

		@Test
		void parameterInjectionByType(CustomType customType) {
			assertNotNull(customType);
		}

		@Test
		void parameterInjectionByAnnotation(@CustomAnnotation String value) {
			assertNotNull(value);
		}

		// some overloaded methods

		@Test
		void overloadedName() {
			assertTrue(true);
		}

		@Test
		void overloadedName(CustomType customType) {
			assertNotNull(customType);
		}

		@Test
		void overloadedName(CustomType customType, @CustomAnnotation String value) {
			assertNotNull(customType);
			assertNotNull(value);
		}
	}

	@ExtendWith(NullIntegerParameterResolver.class)
	private static class NullMethodInjectionTestCase {

		@Test
		void injectWrapper(Integer number) {
			assertNull(number);
		}

		@Test
		void injectPrimitive(int number) {
			// should never be invoked since an int cannot be null
		}

	}

	@ExtendWith(PrimitiveIntegerParameterResolver.class)
	private static class PrimitiveIntegerMethodInjectionTestCase {

		@Test
		void intPrimitive(int i) {
			assertEquals(42, i);
		}

	}

	@ExtendWith(PrimitiveArrayParameterResolver.class)
	private static class PrimitiveArrayMethodInjectionTestCase {

		@Test
		void primitiveArray(int... ints) {
			assertEquals(Arrays.toString(new int[] { 1, 2, 3 }), Arrays.toString(ints));
		}

	}

	@ExtendWith(NumberParameterResolver.class)
	private static class PotentiallyIncompatibleTypeMethodInjectionTestCase {

		@Test
		void numberParameterInjection(Number number) {
			assertEquals(new Integer(42), number);
		}

		@Test
		void integerParameterInjection(Integer number) {
			assertEquals(new Integer(42), number);
		}

		/**
		 * This test must fail, since {@link Double} is a {@link Number} but not an {@link Integer}.
		 * @see NumberParameterResolver
		 */
		@Test
		void doubleParameterInjection(Double number) {
			/* no-op */
		}

	}

	private static class BeforeAndAfterMethodInjectionTestCase {

		@BeforeEach
		void before(TestInfo testInfo) {
			assertEquals("custom name", testInfo.getDisplayName());
		}

		@Test
		@DisplayName("custom name")
		void customNamedTest() {
		}

		@AfterEach
		void after(TestInfo testInfo) {
			assertEquals("custom name", testInfo.getDisplayName());
		}
	}

	@DisplayName("custom class name")
	private static class BeforeAndAfterAllMethodInjectionTestCase {

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			assertEquals("custom class name", testInfo.getDisplayName());
		}

		@Test
		void aTest() {
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			assertEquals("custom class name", testInfo.getDisplayName());
		}
	}

	private static class ExtendWithOnMethodTestCase {

		@Test
		@ExtendWith(CustomTypeParameterResolver.class)
		@ExtendWith(CustomAnnotationParameterResolver.class)
		void testMethodWithExtensionAnnotation(CustomType customType, @CustomAnnotation String value) {
			assertNotNull(customType);
			assertNotNull(value);
		}
	}

}
